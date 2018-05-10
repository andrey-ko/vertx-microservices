package org.andreyko.vertx.microservices.health.check.service

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