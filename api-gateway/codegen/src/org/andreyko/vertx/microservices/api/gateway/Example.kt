@file:JvmName("Example")
package org.andreyko.vertx.microservices.api.gateway

import java.nio.file.*

fun main(args: Array<String>) {
  
  Paths.get("services/gtfs-lookup/.tests-data").toFile().deleteRecursively()
  
  val cfg = AppConfig().apply {
    pckg = javaClass.`package`.name
    spec = "services/gtfs-lookup/spec.openapi.yaml"
    out = "services/gtfs-lookup/.tests-data"
    api = "gtfs-lookup-api"
  }
  
  val env = ICodeGenEnv.Default(cfg)
  ModelsCodeGen.run(env)
  ApiCodeGen.run(env)
}
