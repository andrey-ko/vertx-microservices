package org.andreyko.vertx.microservices.health.check.service;

import io.vertx.codegen.annotations.*
import io.vertx.core.json.*

@DataObject(generateConverter = true)
class ServiceStatus() {
  
  var serviceName: String? = null
  var status: String? = null
  
  constructor(json: JsonObject): this() {
    ServiceStatusConverter.fromJson(json, this)
  }
  
  fun toJson(): JsonObject {
    val json = JsonObject()
    ServiceStatusConverter.toJson(this, json)
    return json
  }
  
}