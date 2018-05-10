package org.andreyko.vertx.microservices.gtfs.lookup.service

import dagger.*
import io.vertx.core.*
import org.slf4j.*
import javax.inject.*

@Module
class GtfsLookupModule {
  var log = LoggerFactory.getLogger(javaClass)
  
  @Provides
  @Singleton
  fun provideClient(vertx: Vertx): IGtfsLookupService {
    log.info("creating gtfs-lookup client...")
    return IGtfsLookupServiceVertxEBProxy(vertx, address)
  }
  
  companion object {
    const val address = "gtfs-lookup-service"
  }
}