package org.andreyko.vertx.microservices.health.check.service;

import com.satori.libs.vertx.kotlin.*
import io.vertx.codegen.annotations.*
import io.vertx.core.*

@ProxyGen
@VertxGen
interface IHealthCheckService {
  fun status(resultHandler: VxAsyncHandler<String>)
  fun getServiceStatuses(resultHandler: VxAsyncHandler<List<ServiceStatus>>)
  fun clusterInfo(resultHandler: VxAsyncHandler<ClusterInfo>)
}