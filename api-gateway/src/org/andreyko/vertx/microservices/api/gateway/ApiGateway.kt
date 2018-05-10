package org.andreyko.vertx.microservices.api.gateway

import com.satori.libs.async.api.*
import com.satori.libs.async.core.*
import com.satori.libs.async.kotlin.*
import com.satori.libs.vertx.kotlin.*
import org.andreyko.vertx.microservices.gtfs.lookup.service.*
import org.andreyko.vertx.microservices.health.check.service.*
import org.andreyko.vertx.microservices.organization.service.*
import javax.inject.*
import kotlin.coroutines.experimental.*

class ApiGateway @Inject constructor(
  val healthCheckService: IHealthCheckService,
  val gtfsLookupService: IGtfsLookupService,
  val organizationService: IOrganizationService
) : IApiGateway {
  
  fun start() {
  
  }
  
  fun stop() {
  }
  
  override fun getAgencies() = future {
    val allAgencies = vxAwait<List<org.andreyko.vertx.microservices.gtfs.lookup.service.Agency>> {
      gtfsLookupService.getAgencies(IdSelector("all", null), it)
    }
    return@future allAgencies.map { a ->
      Agency().apply {
        name = a.name
        id = a.id
      }
    }
  }
  
  override fun getAgencyById(agencyId: String?): IAsyncFuture<Agency> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
  
  override fun getAgenciesByOrgId(orgId: String?) = future {
    val allAgencies = vxAwait<List<org.andreyko.vertx.microservices.gtfs.lookup.service.Agency>> {
      gtfsLookupService.getAgencies(IdSelector("organization-id", orgId), it)
    }
    return@future allAgencies.map { a ->
      Agency().apply {
        name = a.name
        id = a.id
      }
    }
  }
  
  override fun healthCheck() = future {
    vxAwait<List<ServiceStatus>> {rh->
      healthCheckService.getServiceStatuses(rh)
    }.map{
      HealthStatus().apply {
        service = it.serviceName
        status = it.status
      }
    }
  }
}