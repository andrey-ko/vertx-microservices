package org.andreyko.vertx.microservices.api.gateway

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.*
import com.satori.codegen.utils.*
import com.satori.libs.common.kotlin.*
import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.media.*
import io.swagger.v3.parser.*

interface ICodeGenEnv {
  val cfg: AppConfig
  val spec: OpenAPI
  val fmt: ICodeFormatter get() = CodeFormatter
  val mapper: ObjectMapper
  
  fun getSchema(ref: String): Schema<*> {
    if (!ref.startsWith("#/components/schemas/")) {
      throw Exception("invalid schema ref: '$ref'")
    }
    val name = ref.removePrefix("#/components/schemas/")
    val schema = spec.components.schemas[name] ?: throw Exception("can't resolve ref: '$ref'")
    return schema
  }
  
  class Default(cfg: AppConfig) : ICodeGenEnv {
    override val cfg = cfg
    override val spec: OpenAPI
    override val mapper: ObjectMapper = YAMLMapper()
    
    init {
      spec = file(cfg.spec!!) {
        read { inputStream ->
          val s = OpenAPIV3Parser().readWithInfo(
            mapper.readTree(inputStream)
          ).openAPI
          val resolver = OpenAPIResolver(s, null, null)
          resolver.resolve()
        }
      }
      spec.components.schemas.forEach { name, schema ->
        schema.name = name
      }
    }
  }
  
  companion object {
    fun default(cfg: AppConfig, block: ICodeGenEnv.() -> Unit) {
      block(Default(cfg))
    }
  }
}
