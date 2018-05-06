package org.andreyko.vertx.microservices.health.check.service;

import io.vertx.codegen.annotations.*
import io.vertx.core.json.*

@DataObject(generateConverter = true)
class ClusterInfo() {
  
  var members: List<ClusterMemberInfo>? = null
  
  constructor(json: JsonObject) : this() {
    ClusterInfoConverter.fromJson(json, this)
  }
  
  fun toJson(): JsonObject {
    val json = JsonObject()
    ClusterInfoConverter.toJson(this, json)
    return json
  }
  
}