package org.andreyko.vertx.microservices.gtfs.lookup.service;

import io.vertx.codegen.annotations.*
import io.vertx.core.json.*
import java.nio.file.*

@DataObject(generateConverter = true)
class IdSelector() {
  var kind: String? = null
  var value: String? = null
  
  constructor(json: JsonObject) : this() {
    IdSelectorConverter.fromJson(json, this)
  }
  
  constructor(kind: String, value: String?): this(){
    this.kind = kind
    this.value = value
  }
  
  fun toJson(): JsonObject {
    val json = JsonObject()
    IdSelectorConverter.toJson(this, json)
    return json
  }
}