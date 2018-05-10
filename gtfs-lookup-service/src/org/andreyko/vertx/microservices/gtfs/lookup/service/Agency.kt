package org.andreyko.vertx.microservices.gtfs.lookup.service;

import io.vertx.codegen.annotations.*
import io.vertx.core.json.*

@DataObject(generateConverter = true)
class Agency() {
  var id: String? = null
  var name: String? = null
  var organizationId: String? = null
  
  constructor(json: JsonObject) : this() {
    AgencyConverter.fromJson(json, this)
  }
  
  fun toJson(): JsonObject {
    val json = JsonObject()
    AgencyConverter.toJson(this, json)
    return json
  }
}