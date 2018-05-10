package org.andreyko.vertx.microservices.api.gateway

import com.fasterxml.jackson.databind.*
import com.satori.libs.common.kotlin.json.*
import io.vertx.core.*

class RpcResults {
  
  companion object {
    
    val CODE_OK = 200
    val CODE_CREATED = 201
    val CODE_NO_CONTENT = 204
    
    val CODE_NOT_MODIFIED = 304
    
    val CODE_BAD_REQUEST = 400
    val CODE_UNAUTHORIZED = 401
    val CODE_FORBIDDEN = 403
    val CODE_NOT_FOUND = 404
    val CODE_METHOD_NOT_ALLOWED = 405
    val CODE_CONFLICT = 409
    
    val CODE_INTERNAL_SERVER_ERROR = 500
    val CODE_NOT_IMPLEMENTED = 501
    
    val CODE_2xx = 200..299
    
    val MSG_OK = "OK"
    val MSG_CREATED = "Created"
    val MSG_NO_CONTENT = "No Content"
    
    val MSG_BAD_REQUEST = "Bad Request"
    val MSG_UNAUTHORIZED = "Unauthorized"
    val MSG_FORBIDDEN = "Forbidden"
    val MSG_NOT_FOUND = "Not Found"
    val MSG_METHOD_NOT_ALLOWED = "Method Not Allowed"
    val MSG_CONFLICT = "Conflict"
    
    val MSG_INTERNAL_SERVER_ERROR = "Internal Server Error"
    val MSG_NOT_IMPLEMENTED = "Not Implemented"
    
    fun error(code: Int, message: String, error: String? = null, headers: MultiMap? = null): RpcResult {
      val content = error?.let {
        jsonObject {
          field("error", it)
        }
      }
      return RpcResult(code, message, content, headers)
    }
    
    @JvmStatic
    fun notFound() = notFound(null)
    
    @JvmStatic
    fun notFound(error: String?, headers: MultiMap? = null) = error(
      CODE_NOT_FOUND, MSG_NOT_FOUND, error, headers
    )
    
    @JvmStatic
    inline fun notFound(block: RpcErrorResultScope.() -> Unit) = RpcErrorResultScope().run {
      block()
      return@run notFound(error, headers)
    }
    
    @JvmStatic
    inline fun forbidden() = error(
      CODE_FORBIDDEN, MSG_FORBIDDEN
    )
    
    @JvmStatic
    inline fun unauthorized(error: String?, headers: MultiMap? = null) = error(
      CODE_UNAUTHORIZED, MSG_UNAUTHORIZED, error, headers
    )
    
    @JvmStatic
    inline fun unauthorized(headers: MultiMap? = null) = unauthorized(
      null, headers
    )
    
    @JvmStatic
    inline fun unauthorized() = unauthorized(
      null, null
    )
    
    @JvmStatic
    fun methodNotAllowed(error: String? = null, headers: MultiMap? = null) = error(
      CODE_METHOD_NOT_ALLOWED, MSG_METHOD_NOT_ALLOWED, error, headers
    )
    
    @JvmStatic
    fun conflict(error: String? = null, headers: MultiMap? = null) = error(
      CODE_CONFLICT, MSG_CONFLICT, error, headers
    )
    
    @JvmStatic
    fun badRequest(error: String? = null, headers: MultiMap? = null) = error(
      CODE_BAD_REQUEST, MSG_BAD_REQUEST, error, headers
    )
    
    @JvmStatic
    inline fun badRequest(block: RpcErrorResultScope.() -> Unit) = RpcErrorResultScope().run {
      block()
      return@run badRequest(error, headers)
    }
    
    @JvmStatic
    fun internalServerError() = RpcResult(CODE_INTERNAL_SERVER_ERROR, MSG_INTERNAL_SERVER_ERROR)
    
    @JvmStatic
    fun notImplemented() = RpcResult(CODE_NOT_IMPLEMENTED, MSG_NOT_IMPLEMENTED)
    
    @JvmStatic
    fun ok(content: JsonNode? = null) = RpcResult(CODE_OK, MSG_OK, content)
    
    @JvmStatic
    fun ok(block: JsonObjectBuilderScope.() -> Unit): RpcResult {
      return ok(jsonObject(block))
    }
    
    @JvmStatic
    fun created(headers: MultiMap? = null) = RpcResult(CODE_CREATED, MSG_CREATED, null, headers)
    
    @JvmStatic
    fun noContent(headers: MultiMap? = null) = RpcResult(CODE_NO_CONTENT, MSG_NO_CONTENT, null, headers)
    
    @JvmStatic
    inline fun noContent(block: RpcNoContentScope.() -> Unit) = RpcNoContentScope().run {
      block()
      return@run noContent(headers)
    }
    
  }
  
}

