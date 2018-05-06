package org.andreyko.vertx.microservices.gtfs.lookup.service;

import io.vertx.core.*
import org.slf4j.*

class GtfsLookupService : IGtfsLookupService {
  val log = LoggerFactory.getLogger(javaClass)
  override fun status(resultHandler: Handler<AsyncResult<String>>) {
    log.info("status request received")
    resultHandler.handle(Future.succeededFuture("ok"))
  }
}