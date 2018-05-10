package org.andreyko.vertx.microservices.health.check.service;

import com.hazelcast.core.*
import com.hazelcast.core.MultiMap
import com.satori.libs.async.core.*
import com.satori.libs.async.kotlin.*
import com.satori.libs.vertx.kotlin.*
import io.vertx.core.*
import org.andreyko.vertx.microservices.gtfs.lookup.service.*
import org.andreyko.vertx.microservices.organization.service.*
import org.slf4j.*
import java.util.HashMap
import javax.inject.*
import kotlin.collections.ArrayList
import kotlin.collections.component1
import kotlin.collections.component2

class HealthCheckService @Inject constructor(
  val hz: HazelcastInstance,
  val vertx: Vertx,
  val gtfsLookupService: IGtfsLookupService,
  val organizationService: IOrganizationService
) : IHealthCheckService {
  
  val log = LoggerFactory.getLogger(javaClass)
  val delay = 1000L
  var timerId = INVALID_TIMER_ID
  val status = HashMap<String, String>()
  
  fun start() {
    status.clear()
    timerId = vertx.setTimer(delay) { doCheck() }
  }
  
  fun stop() {
    if (timerId != INVALID_TIMER_ID) {
      vertx.cancelTimer(timerId)
      timerId = INVALID_TIMER_ID
    }
  }
  
  override fun status(resultHandler: VxAsyncHandler<String>) {
    resultHandler.handle(Future.succeededFuture("ok"))
  }
  
  override fun getServiceStatuses(resultHandler: VxAsyncHandler<List<ServiceStatus>>) {
    resultHandler.handle(Future.succeededFuture(status.mapTo(ArrayList()) { (k, v) ->
      ServiceStatus().apply {
        serviceName = k
        status = v
      }
    }))
  }
  
  override fun clusterInfo(resultHandler: VxAsyncHandler<ClusterInfo>) {
    val hzCluster = hz.cluster
    val members = ArrayList<ClusterMemberInfo>()
    for (hzMember in hzCluster.members) {
      members.addNew {
        address = hzMember.address.toString()
        uuid = hzMember.uuid
        version = hzMember.version.toString()
      }
    }
    resultHandler.handle(Future.succeededFuture(ClusterInfo().apply {
      this.members = members
    }))
  }
  
  fun doCheck() {
    
    val afj = AsyncForkJoin()
    
    afj.fork().apply {
      gtfsLookupService.status(Handler { ar ->
        if (!ar.succeeded()) {
          log.warn("request failed", ar.cause())
          status.put("gtfs-lookup-service", "failure: ${ar.cause()}")
          fail(ar.cause())
          return@Handler
        }
        log.info("reply message: '${ar.result()}'")
        status.put("gtfs-lookup-service", "healthy")
        succeed()
      })
    }
    
    afj.fork().apply {
      organizationService.status(Handler { ar ->
        if (!ar.succeeded()) {
          log.warn("request failed", ar.cause())
          status.put("organization-service", "failure: ${ar.cause()}")
          fail(ar.cause())
          return@Handler
        }
        log.info("reply message: '${ar.result()}'")
        status.put("organization-service", "healthy")
        succeed()
      })
    }
  
    afj.fork().apply {
      status(Handler { ar ->
        if (!ar.succeeded()) {
          log.warn("request failed", ar.cause())
          status.put("health-check-service", "failure: ${ar.cause()}")
          fail(ar.cause())
          return@Handler
        }
        log.info("reply message: '${ar.result()}'")
        status.put("health-check-service", "healthy")
        succeed()
      })
    }
    
    afj.join().onCompleted { ar ->
      vertx.setTimer(delay) { doCheck() }
    }
    
    val hzCluster = hz.cluster
    println("showing cluster members....")
    for (hzMember in hzCluster.members) {
      println("address: ${hzMember.address},  uuid: ${hzMember.uuid}, v${hzMember.version}")
    }
    println("showing hazelcast information....")
    for (distObj in hz.distributedObjects) {
      println("-------------------------- ${distObj.javaClass.canonicalName} --------------------------")
      println("- name: ${distObj.name}")
      println("- partition key: ${distObj.partitionKey}")
      println("- service name: ${distObj.serviceName}")
      if (distObj is IMap<*, *>) {
        println("- size: ${distObj.size}")
        distObj.forEach { (k, v) ->
          println("- [$k]: $v")
        }
      }
      if (distObj is MultiMap<*, *>) {
        println("- size: ${distObj.size()}")
        distObj.entrySet().forEach { (k, v) ->
          println("- [$k]: $v")
        }
      }
    }
    println("deployments: ${vertx.deploymentIDs()}")
  }
  
  inline fun <reified T> ArrayList<T>.addNew(configure: T.() -> Unit) {
    add(T::class.java.newInstance().apply(configure))
  }
  
  companion object {
    val INVALID_TIMER_ID = -1L
  }
}