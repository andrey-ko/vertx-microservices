@file: JvmName("App")
package org.andreyko.vertx.microservices.api.gateway

import com.satori.libs.common.kotlin.json.*

object App : IJsonContext by DefaultJsonContext {
  
  @JvmStatic
  fun main(vararg args: String) {
    println("openapi-models-gen version: 0.0")
    
    val cfg = jsonObject {
      val itor = args.iterator()
      while (true) {
        val name = if (itor.hasNext()) itor.next() else break
        val value = if (itor.hasNext()) itor.next() else throw Exception("argument value is missing")
        if (!name.startsWith("--")) throw Exception("argument '$name' should start with '--'")
        field(name, value)
      }
    }.scope { toValue<AppConfig>() }
    
    val env = ICodeGenEnv.Default(cfg)
    ModelsCodeGen.run(env)
    ApiCodeGen.run(env)
    RouterCodeGen.run(env)
  }
  
}
