package org.andreyko.vertx.microservices.api.gateway

import io.vertx.core.*
import io.vertx.core.http.*
import org.slf4j.*

class RpcServerHandler : Handler<HttpServerRequest> {
  
  override fun handle(request: HttpServerRequest) {
  
  }
  
  companion object {
    val log = LoggerFactory.getLogger(RpcServerHandler::class.java)
  }
}
