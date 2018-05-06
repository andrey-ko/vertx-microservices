package org.andreyko.vertx.microservices.gtfs.lookup.service

import com.hazelcast.core.*
import io.vertx.core.*
import io.vertx.core.json.*
import io.vertx.servicediscovery.*
import io.vertx.servicediscovery.types.*
import io.vertx.serviceproxy.*
import org.slf4j.*

class GtfsLookupVerticle(val hzInstance: HazelcastInstance) : AbstractVerticle() {
  val log = LoggerFactory.getLogger(javaClass)
  
  var discovery: ServiceDiscovery? = null
  
  override fun start(future: Future<Void>) {
    log.info("started (deploymentId = ${deploymentID()})")
    
    vertx.eventBus().consumer<JsonObject>("vertx.discovery.announce") { msg ->
      log.info("discovery message received: ${msg.body()}")
      msg.reply(JsonObject()
        .put("reply", System.identityHashCode(this))
      )
    }
    
    // create service
    val service = GtfsLookupService()
    
    // register the service proxy on the event bus
    ProxyHelper.registerService(IGtfsLookupService::class.java, vertx, service, "gtfs-lookup-service")
    
    // publish service discovery record
    val discovery = ServiceDiscovery.create(vertx)
    this.discovery = discovery
    val record = EventBusService.createRecord("gtfs-lookup-service", "gtfs-lookup-service", IGtfsLookupService::class.java)
    discovery.publish(record, { ar ->
      if (!ar.succeeded()) {
        // publication failed
        log.error("failed to publish discovery record", ar.cause())
        future.fail(ar.cause())
        vertx.close()
        return@publish
      }
      log.info("discovery record successfully published")
      future.complete()
    })
    
  }
  
  override fun stop(future: Future<Void>) {
    log.info("stopped (deploymentId = ${deploymentID()}")
    //discovery?.unpublish()
    discovery?.close()
    future.complete()
  }
}