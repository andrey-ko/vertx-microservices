package org.andreyko.vertx.microservices.gtfs.lookup.service;

import com.satori.libs.vertx.kotlin.*
import io.vertx.codegen.annotations.*;
import io.vertx.core.*;

@ProxyGen
@VertxGen
interface IGtfsLookupService {
  fun status(resultHandler: VxAsyncHandler<String>)
  fun getAgencies(selector: IdSelector, resultHandler: VxAsyncHandler<List<Agency>>)
}