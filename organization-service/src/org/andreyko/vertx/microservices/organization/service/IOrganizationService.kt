package org.andreyko.vertx.microservices.organization.service;

import io.vertx.codegen.annotations.*
import io.vertx.core.*

@ProxyGen
@VertxGen
interface IOrganizationService {
  fun status(resultHandler: Handler<AsyncResult<String>>)
}