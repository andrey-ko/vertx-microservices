package org.andreyko.vertx.microservices.organization.service;

import io.vertx.core.*

class OrganizationService : IOrganizationService {
  override fun status(resultHandler: Handler<AsyncResult<String>>) {
    resultHandler.handle(Future.succeededFuture("ok"))
  }
}