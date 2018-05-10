package org.andreyko.vertx.microservices.api.gateway

import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.media.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RouterCodeGen(env: ICodeGenEnv) : ICodeGenEnv by env {
  
  var id = 0;
  
  fun run() {
    
    val className = "${fmt.className(cfg.api)}Router"
    val model = RouterModel().apply {
      this.className = className
      this.apiInterface = "I${fmt.className(cfg.api)}"
      this.packageName = cfg.pckg
    }
    
    data class Rec(
      val model: BranchModel,
      val children: HashMap<String, Rec> = HashMap()
    );
    
    model.branch = BranchModel()
    val map = Rec(model.branch)
    
    spec.paths.forEach { path, pathItem ->
      val pitems = ArrayList<String>()
      val tok = StringTokenizer(path, "/")
      while (tok.hasMoreTokens()) {
        pitems.add(tok.nextToken())
      }
      var m = map
      var c = 0;
      for (i in pitems) {
        if (i.startsWith("{")) {
          if (!i.endsWith("}")) throw Exception("'}' expected in the end of '$i'")
          val varName = "_${fmt.varName(i.removePrefix("{").removeSuffix("}"))}"
          if (c < m.model.variables.size) {
            if (m.model.variables[c] != varName) throw Exception("invalid routing topology")
            c += 1
            continue
          }
          m.model.addVariablesItem(varName)
        } else {
          if (m.model.variables.size != c) throw Exception("'}' expected in the end of '$i'")
          c = 0;
          m = m.children.computeIfAbsent(i) { k ->
            val x = BranchModel()
            m.model.addChildrenItem().apply {
              value = i
              branch = x
            }
            Rec(x)
          }
        }
      }
      pathItem.readOperationsMap().forEach { (method, op) ->
        validateOperationResponse(op);
        m.model.hasOperations = true
        m.model.addMethodsItem().apply {
          name = method.name
          operationId = fmt.methodName(op.operationId)
          contentType = "application/json"
          op.parameters?.forEach { p ->
            when (p.`in`) {
              "path" -> addArgsItem("_${fmt.varName(p.name)}")
              "query" -> {
                val varName = "_${fmt.varName(p.name)}"
                addQparamsItem().apply {
                  this.varName = varName
                  this.schemaName = p.name
                  this.type = resolveQparamType(p.schema)
                }
                addArgsItem("_${fmt.varName(p.name)}")
              }
              else -> throw Exception("'${p.`in`}' not supported")
            }
          }
          
        }
      }
      
    }
    
    writeJavaClass(className) { w ->
      RouterTemplate.render(w, model)
    }
    
  }
  
  fun validateOperationResponse(op: Operation): Schema<*> {
    if (op.responses.size == 2) {
      if (op.responses.default == null) throw Exception("operation should have exactly one non-default response")
    } else if (op.responses.size != 1) {
      throw Exception("operation should have exactly one non-default response")
    }
    val response = op.responses["200"] ?: throw Exception("200 response is expected")
    val content = response.content["application/json"] ?: throw Exception("'application/json' content is expected")
    return content.schema
  }
  
  fun resolveQparamType(schema: Schema<*>): String {
    if (schema.`$ref` != null) throw Exception("refs not supported")
    if (schema.enum != null) throw Exception("enums not supported")
    when (schema) {
      is StringSchema -> return "String"
      is IntegerSchema -> when (schema.format) {
        null -> return "Integer"
        "int32" -> return "Integer"
        "int64" -> return "Long"
        else -> throw Exception("unsupported type '${schema.type}:${schema.format}'")
      }
      is NumberSchema -> when (schema.format) {
        null -> return "Double"
        "float" -> return "Float"
        "double" -> return "Double"
        else -> throw Exception("unsupported type '${schema.type}:${schema.format}'")
      }
      is BooleanSchema -> return "Boolean"
      is DateTimeSchema -> return "OffsetDateTime"
      else -> throw Exception("unsupported type '${schema.type}:${schema.format}'")
    }
  }
  
  companion object {
    
    fun run(env: ICodeGenEnv) {
      RouterCodeGen(env).run()
    }
    
  }
}
