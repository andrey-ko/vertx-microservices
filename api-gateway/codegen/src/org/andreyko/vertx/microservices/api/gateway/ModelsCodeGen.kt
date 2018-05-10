package org.andreyko.vertx.microservices.api.gateway

import io.swagger.v3.oas.models.media.*

class ModelsCodeGen(env: ICodeGenEnv) : ICodeGenEnv by env {
  
  fun run() {
    
    spec.components.schemas.forEach { name, schema ->
      val className = className(name)
      val entityModel = generateModel(className, schema)
      if (entityModel != null) {
        entityModel.isStatic = false
        val fileModel = TypeFileModel().apply {
          packageName = cfg.pckg
          type = entityModel
        }
        writeJavaClass(className) {
          TypeFileTemplate.render(it, fileModel)
        }
      }
    }
  }
  
  fun generateModel(className: String, schema: Schema<*>): TypeModel? {
    if (schema.enum != null) {
      if (schema !is StringSchema) throw Exception("only string enums are supported")
      // enum class
      return generateEnum(className, schema)
    }
    if (schema is ObjectSchema) return generateType(className, schema)
    if (schema is ComposedSchema) return generateType(className, schema)
    
    throw Exception("unsupported type '${schema.type}:${schema.format}'")
  }
  
  fun generateType(className: String, schema: ObjectSchema): TypeModel {
    val model = TypeModel()
    model.isStatic = true
    model.className = className
    model.isClass = true
    schema.properties?.forEach{(pname, pschema)->
      model.addPropertiesItem().apply {
        schemaName = pname
        varName = fmt.varName(pname)
        type = resolveType(pschema, pname, model)
      }
    }
    return model
  }
  
  fun generateType(className: String, schema: ComposedSchema): TypeModel {
    if (schema.anyOf != null) throw Exception("anyOf composition is not supported")
    if (schema.oneOf != null) throw Exception("oneOf composition is not supported")
    val allOf = schema.allOf ?: throw Exception("only allOf composition is supported")
    
    val model = TypeModel()
    model.isStatic = true
    model.className = className
    model.isClass = true
    
    //var baseType:String? = null
    for (partialSchema in allOf) {
      val ref = partialSchema.`$ref`
      if (ref != null) {
        if (model.base != null) throw Exception("multiple refs are not supported in allOf composition")
        model.base = getSchema(ref).name
        continue
      }
      if (partialSchema !is ObjectSchema) throw Exception("refs and objects are supported in allOf composition")
      partialSchema.properties.forEach { pname, pschema ->
        model.addPropertiesItem().apply {
          schemaName = pname
          varName = fmt.varName(pname)
          type = resolveType(pschema, pname, model)
        }
      }
    }
    return model
  }
  
  fun generateEnum(className: String, schema: StringSchema): TypeModel {
    val model = TypeModel()
    
    model.className = className
    model.isEnum = true
    for (v in schema.enum) {
      model.addOptionsItem().apply {
        name = fmt.constName(v)
        value = v
      }
    }
    return model
  }
  
  fun className(name: String): String {
    return "${cfg.prefix}${fmt.className(name)}${cfg.suffix}"
  }
  
  fun resolveRef(ref: String, parent: TypeModel): String {
    val schema = getSchema(ref)
    schema.`$ref`?.let { r ->
      return resolveRef(r, parent)
    }
    return schema.name
    //return resolveType(schema, schema.name, parent)
  }
  
  fun resolveType(schema: Schema<*>, pname: String, parent: TypeModel): String {
    schema.`$ref`?.let { ref ->
      return resolveRef(ref, parent)
    }
    
    if (schema.enum != null) {
      if (schema !is StringSchema) throw Exception("only string enums are supported")
      if (schema.format !== null) throw Exception("only string enums are supported")
      // enum class
      val className = fmt.className(pname)
      parent.addTypesItem().apply {
        type = generateEnum(className, schema)
        type.isStatic = true
        return className
      }
    }
    val nullable = schema.nullable ?: true
    when (schema) {
      is ArraySchema -> return "List<${resolveType(schema.items, "$pname-item", parent)}>"
      is ObjectSchema -> {
        // nested class
        val className = fmt.className(pname)
        parent.addTypesItem().apply {
          type = generateType(className, schema)
          type.isStatic = true
        }
        return className
      }
      is StringSchema -> return "String"
      is IntegerSchema -> when (schema.format) {
        null -> return if (nullable) "Integer" else "int"
        "int32" -> return if (nullable) "Integer" else "int"
        "int64" -> return if (nullable) "Long" else "long"
        else -> throw Exception("unsupported type '${schema.type}:${schema.format}'")
      }
      is NumberSchema -> when (schema.format) {
        null -> return if (nullable) "Double" else "double"
        "float" -> return if (nullable) "Float" else "float"
        "double" -> return if (nullable) "Double" else "double"
        else -> throw Exception("unsupported type '${schema.type}:${schema.format}'")
      }
      is BooleanSchema -> return "Boolean"
      is DateTimeSchema -> return "OffsetDateTime"
      is ComposedSchema -> {
        // nested class
        val className = fmt.className(pname)
        parent.addTypesItem().apply {
          type = generateType(className, schema)
          type.isStatic = true
        }
        return className
      }
      else -> throw Exception("unsupported type '${schema.type}:${schema.format}'")
    }
  }
  
  companion object {
    
    fun run(env: ICodeGenEnv) {
      ModelsCodeGen(env).run()
    }
    
  }
}
