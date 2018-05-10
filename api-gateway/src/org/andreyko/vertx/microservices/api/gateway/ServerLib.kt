package org.andreyko.vertx.microservices.api.gateway

import com.satori.libs.async.kotlin.*
import com.satori.libs.common.kotlin.json.*
import com.satori.libs.vertx.kotlin.*
import io.vertx.core.*
import io.vertx.core.http.*
import org.slf4j.*

fun HttpServerResponse.end(rpcResult: RpcResult) {
  statusCode = rpcResult.code
  statusMessage = rpcResult.message
  
  rpcResult.headers?.forEach { (k, v) ->
    putHeader(k, v)
  }
  
  val content = rpcResult.content
  if (content != null) {
    putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    end(content.toString())
  } else {
    end()
  }
}

fun Vertx.server(opts: HttpServerOptions = HttpServerOptions(), handler: suspend VxFutureScope.(RpcRequest) -> RpcResult): HttpServer {
  val server = createHttpServer(opts)
  val log = LoggerFactory.getLogger("server")
  server.connectionHandler { con ->
    log.info("${con.remoteAddress()} connected")
    con.closeHandler {
      log.info("${con.remoteAddress()} disconnected")
    }
  }
  server.requestHandler { req ->
    log.info(
      "${req.remoteAddress()}: ${req.method()} ${req.uri()}"
    )
    req.response()
      .putHeader(HttpHeaders.SERVER, "vertx")
      .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
    req.bodyHandler { buffer ->
      try {
        future(this) {
          val content = if (buffer.length() > 0) {
            try {
              DefaultJsonContext.jsonParseAsTree(buffer)
            } catch (error: Throwable) {
              log.warn("bad content", error)
              return@future RpcResults.badRequest()
            }
          } else {
            null
          }
          return@future VxFutureScope(this).handler(RpcRequest(
            req.method(), req.path(), content, req.params(), req.headers()
          ))
        }.onCompleted { ar ->
          if (!ar.isSucceeded) {
            val e = ar.error
            when (e) {
              is RpcException -> {
                req.response().end(e.toRpcResult())
              }
              else -> {
                log.error("unhandled exception", ar.error)
                req.response().end(RpcResults.internalServerError())
              }
            }
            return@onCompleted
          }
          req.response().end(ar.value)
        }
      } catch (e: RpcException) {
        req.response().end(e.toRpcResult())
      } catch (error: Throwable) {
        log.error("unhandled exception", error)
        req.response().end(RpcResults.internalServerError())
      }
      
    }
  }
  return server
}
