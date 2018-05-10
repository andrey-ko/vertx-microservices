package org.andreyko.vertx.microservices.organization.service

import io.vertx.core.*
import io.vertx.core.eventbus.*
import io.vertx.core.json.*
import io.vertx.serviceproxy.*
import org.slf4j.*

class OrganizationVerticle() : AbstractVerticle() {
  val log = LoggerFactory.getLogger(javaClass)
  var binder: ServiceBinder? = null
  var serviceHandler: MessageConsumer<JsonObject>? = null
  
  override fun start(future: Future<Void>) {
    log.info("started (deploymentId = ${deploymentID()})")
    
    // create service binder
    val binder = ServiceBinder(vertx)
    this.binder = binder
    
    // create service
    val service = OrganizationService()
    
    // Register the handler
    val serviceHandler = binder
      .setAddress(OrganizationModule.address)
      .register(IOrganizationService::class.java, service)
    
    this.serviceHandler = serviceHandler
  }
  
  override fun stop(future: Future<Void>) {
    log.info("stopping verticle (deploymentId = ${deploymentID()})...")
    val binder = binder
    if (binder != null) {
      this.binder = null
      val serviceHandler = serviceHandler
      if (serviceHandler != null) {
        this.serviceHandler = null
        binder.unregister(serviceHandler)
      }
    }
    future.complete()
  }
}