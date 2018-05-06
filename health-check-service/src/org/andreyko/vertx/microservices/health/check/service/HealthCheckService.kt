package org.andreyko.vertx.microservices.health.check.service;

import com.hazelcast.core.*
import io.vertx.core.*

class HealthCheckService(val hzInstance: HazelcastInstance) : IHealthCheckService {
  override fun status(resultHandler: Handler<AsyncResult<String>>) {
    resultHandler.handle(Future.succeededFuture("ok"))
  }
  
  inline fun<reified T> ArrayList<T>.addNew(configure: T.()->Unit){
    add(T::class.java.newInstance().apply(configure))
  }
  
  override fun clusterInfo(resultHandler: Handler<AsyncResult<ClusterInfo>>) {
    val hzCluster = hzInstance.cluster
    val members = ArrayList<ClusterMemberInfo>()
    for(hzMember in hzCluster.members){
      members.addNew{
        address = hzMember.address.toString()
        uuid = hzMember.uuid
        version = hzMember.version.toString()
      }
    }
    resultHandler.handle(Future.succeededFuture(ClusterInfo().apply {
      this.members = members
    }))
  }
}