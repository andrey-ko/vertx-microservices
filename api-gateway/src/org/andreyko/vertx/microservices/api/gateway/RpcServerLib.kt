package org.andreyko.vertx.microservices.api.gateway

import com.satori.libs.common.kotlin.json.*
import com.satori.libs.vertx.kotlin.*
import io.vertx.core.*
import io.vertx.core.http.*

fun Vertx.rpcServer(opts: HttpServerOptions = HttpServerOptions(), builder: RpcServerScope.() -> Unit): HttpServer {
  val services = HashMap<String, suspend VxFutureScope.(RpcRequest) -> RpcResult>()
  RpcServerScope(services).builder()
  return server(opts) { req ->
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
    
    val i = path.indexOf('/', 1)
    val serviceId: String
    val operation: String
    if (i < 0) {
      serviceId = path.substring(1)
      operation = ""
    } else {
      serviceId = path.substring(1, i)
      operation = path.substring(i + 1)
    }
    
    if (serviceId.isEmpty()) {
      when (req.method) {
        HttpMethod.GET -> return@server RpcResults.ok {
          // return status of available services
          services.forEach { path, srv ->
            field(path, jsonObject {
              field("status", "healthy")
            })
          }
        }
        HttpMethod.HEAD -> return@server RpcResults.ok()
        else -> return@server RpcResults.badRequest("only GET & HEAD are allowed")
      }
      
    }
    
    val srv = services[serviceId] ?: return@server RpcResults.notFound("unknown service")
    
    return@server srv(RpcRequest(
      req.method, operation, req.content, req.params, req.headers
    ))
  }
}

/*fun rpcHandler(block: ): IAsyncFuture<RpcResult>{

}*/
