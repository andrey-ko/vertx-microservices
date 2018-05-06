package org.andreyko.vertx.microservices.health.check.service

import com.hazelcast.core.*
import com.hazelcast.core.MultiMap
import io.vertx.core.*
import io.vertx.core.json.*
import io.vertx.servicediscovery.*
import io.vertx.servicediscovery.types.*
import io.vertx.serviceproxy.*
import org.andreyko.vertx.microservices.gtfs.lookup.service.*
import org.andreyko.vertx.microservices.organization.service.*
import org.slf4j.*
import java.util.*

class HealthCheckVerticle(val hzInstance: HazelcastInstance) : AbstractVerticle() {
  val log = LoggerFactory.getLogger(javaClass)
  
  var discovery: ServiceDiscovery? = null
  var gtfsLookupService: IGtfsLookupService? = null
  var organizationService: IOrganizationService? = null
  
  val delay = 1000L
  var timerId = INVALID_TIMER_ID
  
  companion object {
    val INVALID_TIMER_ID = -1L
  }
  
  override fun start(future: Future<Void>) {
    log.info("started (deploymentId = ${deploymentID()})")
    
    val discovery = ServiceDiscovery.create(vertx)
    this.discovery = discovery
    
    vertx.eventBus().consumer<JsonObject>("vertx.discovery.announce") { msg ->
      log.info("discovery message received: ${msg.body()}")
      msg.reply(JsonObject()
        .put("reply", System.identityHashCode(this))
      )
    }
    
    gtfsLookupService = IGtfsLookupServiceVertxEBProxy(vertx, "gtfs-lookup-service")
    organizationService = IOrganizationServiceVertxEBProxy(vertx, "organization-service")
    
    // create service
    val service = HealthCheckService(hzInstance)
    
    // register the service proxy on the event bus
    ProxyHelper.registerService(IHealthCheckService::class.java, vertx, service, "health-check-service")
    
    // publish service discovery record
    val record = EventBusService.createRecord(
      "health-check-service", "health-check-service", IHealthCheckService::class.java
    )
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
    
    timerId = vertx.setPeriodic(delay) { doCheck() }
  }
  
  fun doCheck() {
    val status = HashMap<String, String>()
    gtfsLookupService!!.status(Handler { ar ->
      if (!ar.succeeded()) {
        log.warn("request failed", ar.cause())
        status.put("gtfs-lookup-service", "failure: ${ar.cause()}")
        return@Handler
      }
      log.info("reply message: '${ar.result()}'")
      status.put("gtfs-lookup-service", "healthy")
    })
    
    organizationService!!.status(Handler { ar ->
      if (!ar.succeeded()) {
        log.warn("request failed", ar.cause())
        status.put("gtfs-lookup-service", "failure: ${ar.cause()}")
        return@Handler
      }
      log.info("reply message: '${ar.result()}'")
      status.put("organization-service", "healthy")
    })
    
    discovery!!.getRecords({true}, true) { ar ->
      if (!ar.succeeded()) {
        log.error("failed to retrieve discovery records", ar.cause())
      }
      val records = ar.result()
      for (record in records) {
        log.info("${record.name}")
      }
    }
    val hzCluster = hzInstance.cluster
    println("showing cluster members....")
    for(hzMember in hzInstance.cluster.members){
      println("address: ${hzMember.address},  uuid: ${hzMember.uuid}, v${hzMember.version}")
    }
    println("showing hazelcast information....")
    for (distObj in hzInstance.distributedObjects) {
      println("-------------------------- ${distObj.javaClass.canonicalName} --------------------------")
      println("- name: ${distObj.name}")
      println("- partition key: ${distObj.partitionKey}")
      println("- service name: ${distObj.serviceName}")
      if(distObj is IMap<*,*>){
        println("- size: ${distObj.size}")
        distObj.forEach{(k, v) ->
          println("- [$k]: $v")
        }
      }
      if (distObj is MultiMap<*, *>){
        distObj
        println("- size: ${distObj.size()}")
        distObj.entrySet().forEach{(k, v) ->
          println("- [$k]: $v")
        }
      }
    }
    println("deployments: ${vertx.deploymentIDs()}")
  }
  
  override fun stop(future: Future<Void>) {
    log.info("stopped (deploymentId = ${deploymentID()}")
    gtfsLookupService = null
    organizationService = null
    
    if (timerId != INVALID_TIMER_ID) {
      vertx.cancelTimer(timerId)
      timerId = INVALID_TIMER_ID
    }
    
    //discovery?.unpublish()
    discovery?.close()
    future.complete()
  }
}