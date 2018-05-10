package org.andreyko.vertx.microservices.api.gateway

import io.vertx.core.*
import io.vertx.core.http.*
import org.slf4j.*
import javax.inject.*

class ServerVerticle @Inject constructor(
  val apiGateway: ApiGateway
) : AbstractVerticle() {
  val log = LoggerFactory.getLogger(javaClass)
  var server: HttpServer? = null
  
  override fun start(future: Future<Void>) {
    log.info("starting (deploymentId = ${deploymentID()})...")
    
    val router = ApiGatewayRouter(apiGateway)
    
    val server = vertx.server { req ->
      if (req.method == HttpMethod.OPTIONS) {
        return@server RpcResults.noContent {
          header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "OPTIONS, GET, POST, PUT, PATCH, DELETE, HEAD")
          header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Content-Type")
        }
      }
      
      val allowedRpcMethods = arrayOf(
        HttpMethod.GET, HttpMethod.HEAD,
        HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH,
        HttpMethod.DELETE
      )
      
      if (req.method !in allowedRpcMethods) {
        return@server RpcResults.badRequest("invalid HTTP method")
      }
      
      val path = req.path
      if (!path.startsWith("/")) {
        return@server RpcResults.notFound("HTTP path should start with '/'")
      }
      return@server router.process(req).await()
    }
    server.listen(8400)
    this.server = server
    
    future.complete()
  }
  
  override fun stop(future: Future<Void>) {
    log.info("stopping (deploymentId = ${deploymentID()})...")
    val server = this.server
    this.server = null
    server?.close{ar->
    }
    future.complete()
  }
}