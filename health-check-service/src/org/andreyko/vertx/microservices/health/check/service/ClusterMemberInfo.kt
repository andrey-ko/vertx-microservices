package org.andreyko.vertx.microservices.health.check.service;

import io.vertx.codegen.annotations.*
import io.vertx.core.json.*

@DataObject(generateConverter = true)
class ClusterMemberInfo() {
  
  var address: String? = null
  var uuid: String? = null
  var version: String? = null
  
  constructor(json: JsonObject): this() {
    ClusterMemberInfoConverter.fromJson(json, this)
  }
  
  fun toJson(): JsonObject {
    val json = JsonObject()
    ClusterMemberInfoConverter.toJson(this, json)
    return json
  }
  
}