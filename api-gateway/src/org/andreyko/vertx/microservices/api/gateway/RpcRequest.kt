package org.andreyko.vertx.microservices.api.gateway

import com.fasterxml.jackson.databind.*
import io.vertx.core.*
import io.vertx.core.http.*

class RpcRequest(
  var method: HttpMethod,
  var path: String,
  var content: JsonNode? = null,
  var params: MultiMap? = null,
  var headers: MultiMap? = null
)