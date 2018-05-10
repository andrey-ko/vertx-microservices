package org.andreyko.vertx.microservices.api.gateway

import com.fasterxml.jackson.databind.*
import io.vertx.core.*

open class RpcException(
  val code: Int,
  override val message: String,
  val content: JsonNode? = null,
  val headers: MultiMap? = null
) : Exception(message, null, false, false) {
  fun toRpcResult() = RpcResult(code, message, content, headers)
}

class RpcNotFoundException(
  content: JsonNode? = null,
  headers: MultiMap? = null
) : RpcException(RpcResults.CODE_NOT_FOUND, RpcResults.MSG_NOT_FOUND, content, headers)

class RpcBadRequestException(
  content: JsonNode? = null,
  headers: MultiMap? = null
) : RpcException(RpcResults.CODE_BAD_REQUEST, RpcResults.MSG_BAD_REQUEST, content, headers)

class RpcUnauthorizedException(
  content: JsonNode? = null,
  headers: MultiMap? = null
) : RpcException(RpcResults.CODE_UNAUTHORIZED, RpcResults.MSG_UNAUTHORIZED, content, headers)

class RpcForbiddenException(
  content: JsonNode? = null,
  headers: MultiMap? = null
) : RpcException(RpcResults.CODE_FORBIDDEN, RpcResults.MSG_FORBIDDEN, content, headers)


