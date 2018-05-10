package org.andreyko.vertx.microservices.health.check.service

import io.vertx.core.*
import io.vertx.core.eventbus.*
import io.vertx.core.json.*
import org.andreyko.vertx.microservices.gtfs.lookup.service.*
import org.slf4j.*
import javax.inject.*

class HealthCheckVerticle @Inject constructor(
  val service: HealthCheckService
) : AbstractVerticle() {
  val log = LoggerFactory.getLogger(javaClass)
  var consumer: MessageConsumer<JsonObject>? = null
  var handler: IHealthCheckServiceVertxProxyHandler? = null
  //var service: GtfsLookupService? = null
  
  override fun start(future: Future<Void>) {
    log.info("started (deploymentId = ${deploymentID()})")
  
    // create handler for service
    val handler = IHealthCheckServiceVertxProxyHandler(vertx, service)
    this.handler = handler
    consumer = handler.register(
      vertx.eventBus(), HealthCheckModule.address, null
    )
  
    service.start()
    future.complete()
  }
  
  override fun stop(future: Future<Void>) {
    log.info("stopping verticle (deploymentId = ${deploymentID()})...")
    service.stop()
    val handler = this.handler
    this.handler = null
    val consumer = this.consumer
    this.consumer = null
    
    try {
      handler?.close()
    } catch (ex: Throwable) {
      log.error("failed to close handler", ex)
    }
    
    try {
      consumer?.unregister()
    } catch (ex: Throwable) {
      log.error("failed to unregister consumer", ex)
    }
    future.complete()
  }
}