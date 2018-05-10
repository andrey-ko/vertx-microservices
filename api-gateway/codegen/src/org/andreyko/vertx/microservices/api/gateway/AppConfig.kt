package org.andreyko.vertx.microservices.api.gateway

import com.fasterxml.jackson.annotation.*

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
class AppConfig {
  @JsonProperty("--pckg")
  var pckg: String = ""
  
  @JsonProperty("--spec")
  var spec: String? = null
  
  @JsonProperty("--out")
  var out: String = ""
  
  @JsonProperty("--api")
  var api: String = "Api"
  
  @JsonProperty("--prefix")
  var prefix: String = ""
  
  @JsonProperty("--suffix")
  var suffix: String = ""
}


