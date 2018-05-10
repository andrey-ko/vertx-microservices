package org.andreyko.vertx.microservices.api.gateway

import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.media.*

class ApiCodeGen(env: ICodeGenEnv) : ICodeGenEnv by env {
  
  fun run() {
    
    val className = "I${fmt.className(cfg.api)}"
    val model = ApiModel().apply {
      this.className = className
      this.packageName = cfg.pckg
    }
    
    spec.paths.forEach { path, pathItem ->
      for (op in pathItem.readOperations()) {
        val response = getOpResponse(op)
        if (response is ObjectSchema) {
          throw Exception("inline responses not supported yet")
        }
        op.description?.lines()?.forEach {
        
        }
        model.addOperationsItem().apply {
          methodName = fmt.methodName(op.operationId)
          responseType = resolveType(response, true)
          
          op.parameters?.forEach { p ->
            addArgsItem().apply {
              name = fmt.varName(p.name)
              type = resolveType(p.schema, p.required)
            }
          }
        }
      }
    }
    
    writeJavaClass(className) { w ->
      ApiTemplate.render(w, model)
    }
    
  }
  
  fun getOpResponse(op: Operation): Schema<*> {
    if (op.responses.size == 2) {
      if (op.responses.default == null) throw Exception("operation should have exactly one non-default response")
    } else if (op.responses.size != 1) {
      throw Exception("operation should have exactly one non-default response")
    }
    val response = op.responses.values.filter { v -> v != op.responses.default }.first()
    if (response.content.size != 1) throw Exception("operation should have exactly one content for response")
    
    val content = response.content["application/json"]
    if (content == null) throw Exception("only  one content for response")
    return content.schema
  }
  
  fun resolveRef(ref: String, required: Boolean): String {
    val schema = getSchema(ref)
    return resolveType(schema, required)
  }
  
  fun resolveType(schema: Schema<*>, required: Boolean): String {
    schema.`$ref`?.let { ref ->
      return resolveRef(ref, required)
    }
    schema.name?.let { name ->
      return name
    }
    val nullable = !required || (schema.nullable ?: false)
    when (schema) {
      is StringSchema -> return "String"
      is IntegerSchema -> when (schema.format) {
        null -> return if (nullable) "Integer" else "int"
        "int32" -> return if (nullable) "Integer" else "int"
        "int64" -> return if (nullable) "Long" else "long"
        else -> throw Exception("unsupported type '${schema.type}:${schema.format}'")
      }
      is BooleanSchema -> return if (nullable) "Boolean" else "bool"
      is DateTimeSchema -> return "OffsetDateTime"
      is ArraySchema -> return "List<${resolveType(schema.items, false)}>"
      else -> return fmt.className(schema.name)
    }
  }
  
  companion object {
    
    fun run(env: ICodeGenEnv) {
      ApiCodeGen(env).run()
    }
    
  }
}
