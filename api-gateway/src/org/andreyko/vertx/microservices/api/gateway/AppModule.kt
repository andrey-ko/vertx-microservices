package org.andreyko.vertx.microservices.api.gateway

import com.hazelcast.core.*
import dagger.*
import io.vertx.core.*

@Module
class AppModule(
  val vertx: Vertx,
  val hz: HazelcastInstance
) {
  
  @Provides
  fun provideVertx(): Vertx {
    return vertx
  }
  
  @Provides
  fun provideHz(): HazelcastInstance {
    return hz
  }
}