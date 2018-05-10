package org.andreyko.vertx.microservices.gtfs.lookup.service;

import com.satori.libs.vertx.kotlin.*
import io.vertx.core.*
import org.slf4j.*

class GtfsLookupService : IGtfsLookupService {
  val log = LoggerFactory.getLogger(javaClass)
  override fun status(resultHandler: Handler<AsyncResult<String>>) {
    log.info("status request received")
    resultHandler.handle(Future.succeededFuture("ok"))
  }
  
  
  override fun getAgencies(selector: IdSelector, resultHandler: VxAsyncHandler<List<Agency>>) {
    val kind = selector.kind ?: throw Exception("selector kind not specified")
    log.info("getAgencies...")
    if(kind == "all"){
      resultHandler.complete(listOf(
        Agency().apply {
          id = "RTH"
          name = "RTH"
          organizationId = "AT"
        }
      ))
      return
    }
    
    val value = selector.value ?: throw Exception("selector value not specified")
    when (kind){
      "organization-id" -> {
        // get agency by id
        if(value != "AT")  throw Exception("organization not found")
        resultHandler.complete(listOf(
          Agency().apply {
            id = "RTH"
            name = "RTH"
            organizationId = "AT"
          }
        ))
        return
      }
      else -> throw Exception("unsupported kind")
    }
  }
  
  inline fun<reified T> VxAsyncHandler<T>.complete(value: T){
    handle(VxFuture.succeededFuture(value))
  }
  
  inline fun<reified T> VxAsyncHandler<T>.fail(error: Throwable){
    handle(VxFuture.failedFuture(error))
  }
  
}