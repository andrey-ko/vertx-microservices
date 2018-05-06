package org.andreyko.vertx.microservices.health.check.service

import com.hazelcast.config.*
import io.vertx.core.*
import io.vertx.spi.cluster.hazelcast.*
import org.slf4j.*
import javax.transaction.xa.*

object App {
  val log = LoggerFactory.getLogger(javaClass)
  
  @JvmStatic
  fun main(vararg args: String) {
    val hzCfg = Config()
    //hzCfg.setProperty("hazelcast.logging.type", "slf4j");
    System.setProperty("vertx.hazelcast.async-api", "true")
    
    val clusterManager = HazelcastClusterManager(hzCfg)
    val vertxOptions = VertxOptions().apply {
      this.clusterManager = clusterManager
      this.isClustered = true
    }
    
    Vertx.clusteredVertx(vertxOptions) { ar ->
      if (!ar.succeeded()) {
        log.error("failed to create vertx", ar.cause())
        return@clusteredVertx
      }
      val vertx = ar.result()
      vertx.exceptionHandler {exn->
        vertx.close{ar->
          clusterManager.hazelcastInstance.shutdown()
          System.runFinalization()
          System.exit(-1) // TODO: fix it
        }
      }
      log.info("vertx successfully created")
      val deploymentOptions = DeploymentOptions().apply {
      }
      vertx.deployVerticle(HealthCheckVerticle(clusterManager.hazelcastInstance)) { deploymentResult ->
        if (!deploymentResult.succeeded()) {
          //log.error("failed to deploy verticle", deploymentResult.cause())
        }
        log.info("verticle successfully deployed(deploymentId=${deploymentResult.result()})")
      }
    }
  }
}