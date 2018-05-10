package org.andreyko.vertx.microservices.health.check.service

import dagger.*
import org.andreyko.vertx.microservices.gtfs.lookup.service.*
import org.andreyko.vertx.microservices.organization.service.*
import javax.inject.*

@Singleton
@Component(modules = arrayOf(
  AppModule::class,
  GtfsLookupModule::class,
  OrganizationModule::class
))
interface AppComponent {
  fun newHealthCheckVerticle(): HealthCheckVerticle
}
