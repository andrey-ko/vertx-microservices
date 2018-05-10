package org.andreyko.vertx.microservices.health.check.service

import dagger.*
import io.vertx.core.*
import org.slf4j.*
import javax.inject.*

@Module
class HealthCheckModule {
  var log = LoggerFactory.getLogger(javaClass)
  
  @Provides
  @Singleton
  fun provideClient(vertx: Vertx): IHealthCheckService {
    log.info("creating health-check client...")
    return IHealthCheckServiceVertxEBProxy(vertx, address)
  }
  
  companion object {
    const val address = "health-check-service"
  }
}