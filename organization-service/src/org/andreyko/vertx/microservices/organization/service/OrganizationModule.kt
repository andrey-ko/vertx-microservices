package org.andreyko.vertx.microservices.organization.service

import dagger.*
import io.vertx.core.*
import org.slf4j.*
import javax.inject.*

@Module
class OrganizationModule {
  var log = LoggerFactory.getLogger(javaClass)
  
  @Singleton
  @Provides
  fun provideClient(vertx: Vertx): IOrganizationService {
    log.info("creating organization client")
    return IOrganizationServiceVertxEBProxy(vertx, address)
  }
  
  companion object {
    const val address = "organization-service"
  }
}