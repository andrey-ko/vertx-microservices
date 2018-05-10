package org.andreyko.vertx.microservices.api.gateway

class RpcErrorResultScope : RpcNoContentScope() {
  
  var error: String? = null
  
  fun error(value: String) {
    error = value
  }
}
