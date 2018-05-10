package org.andreyko.vertx.microservices.api.gateway

import dagger.*
import org.andreyko.vertx.microservices.gtfs.lookup.service.*
import org.andreyko.vertx.microservices.health.check.service.*
import org.andreyko.vertx.microservices.organization.service.*
import javax.inject.*

@Singleton
@Component(modules = arrayOf(
  AppModule::class,
  HealthCheckModule::class,
  GtfsLookupModule::class,
  OrganizationModule::class
))
interface AppComponent {
  fun newServerVerticle(): ServerVerticle
}
