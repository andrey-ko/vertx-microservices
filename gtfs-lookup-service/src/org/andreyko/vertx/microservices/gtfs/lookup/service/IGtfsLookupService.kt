package org.andreyko.vertx.microservices.gtfs.lookup.service;

import io.vertx.codegen.annotations.*;
import io.vertx.core.*;

@ProxyGen
@VertxGen
interface IGtfsLookupService {
  fun status(resultHandler: Handler<AsyncResult<String>>);
}