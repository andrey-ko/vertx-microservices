package org.andreyko.vertx.microservices.organization.service;

import io.vertx.core.*
import org.slf4j.*

class OrganizationService : IOrganizationService {
  val log = LoggerFactory.getLogger(javaClass)
  override fun status(resultHandler: Handler<AsyncResult<String>>) {
    log.info("status request received")
    resultHandler.handle(Future.succeededFuture("ok"))
  }
}