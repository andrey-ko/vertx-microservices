package org.andreyko.vertx.microservices.health.check.service;

import io.vertx.codegen.annotations.*
import io.vertx.core.*

@ProxyGen
@VertxGen
interface IHealthCheckService {
  fun status(resultHandler: Handler<AsyncResult<String>>)
  fun clusterInfo(resultHandler: Handler<AsyncResult<ClusterInfo>>)
}