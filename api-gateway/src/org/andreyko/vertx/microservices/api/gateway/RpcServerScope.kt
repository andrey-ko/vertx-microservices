package org.andreyko.vertx.microservices.api.gateway

import com.satori.libs.vertx.kotlin.*

@RpcServerScope.Marker
class RpcServerScope(private val services: HashMap<String, suspend VxFutureScope.(RpcRequest) -> RpcResult>) {
  
  @DslMarker
  annotation class Marker
  
  fun service(name: String, handler: suspend VxFutureScope.(RpcRequest) -> RpcResult) {
    services.put(name, handler)
  }
  
  fun service(name: String, service: IRpcService) {
    services.put(name) { req ->
      service.handle(req).await()
    }
  }
  
}
