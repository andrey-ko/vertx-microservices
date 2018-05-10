package org.andreyko.vertx.microservices.api.gateway

import io.vertx.core.*

open class RpcNoContentScope {
  var headers: MultiMap? = null
  
  private fun createHeadersIfNull(): MultiMap {
    var headers = this.headers
    if (headers === null) {
      headers = MultiMap.caseInsensitiveMultiMap()!!
      this.headers = headers
    }
    return headers
  }
  
  fun header(name: String, value: String) {
    createHeadersIfNull().set(name, value)
  }
  
  fun header(name: CharSequence, value: CharSequence) {
    createHeadersIfNull().set(name, value)
  }
}
