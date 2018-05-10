package org.andreyko.vertx.microservices.gtfs.lookup.service

import com.hazelcast.core.*
import io.vertx.core.*
import io.vertx.core.eventbus.*
import io.vertx.core.json.*
import org.slf4j.*

class GtfsLookupVerticle(val hzInstance: HazelcastInstance) : AbstractVerticle() {
  val log = LoggerFactory.getLogger(javaClass)
  //var consumer: MessageConsumer<JsonObject>? = null
  var handler: IGtfsLookupServiceVertxProxyHandler? = null
  //var service: GtfsLookupService? = null
  
  override fun start(future: Future<Void>) {
    log.info("started (deploymentId = ${deploymentID()})")
    
    // create service
    val service = GtfsLookupService()
    
    // create handler for service
    val handler = IGtfsLookupServiceVertxProxyHandler(vertx, service)
    this.handler = handler
    handler.register(
      vertx.eventBus(), GtfsLookupModule.address, null
    )
    
    future.complete()
  }
  
  override fun stop(future: Future<Void>) {
    log.info("stopping verticle (deploymentId = ${deploymentID()})...")
    val handler = this.handler
    this.handler = null
    
    try {
      handler?.close()
    } catch (ex: Throwable) {
      log.error("failed to close handler", ex)
    }
    
    future.complete()
  }
}