package api

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import play.api.mvc.{AbstractController, ControllerComponents}

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Application @Inject()(system: ActorSystem, cc: ControllerComponents) extends AbstractController(cc) {

  val cluster = Cluster(system)
  system.log.info("Started [" + system + "], cluster.selfAddress = " + cluster.selfMember.address + ")")
  system.log.info(system.name)


  if (!Option(System.getProperty("ENV")).contains("DEV")) {
    // Akka Management hosts the HTTP routes used by bootstrap
    AkkaManagement(system).start()

    // Starting the bootstrap process needs to be done explicitly
    ClusterBootstrap(system).start()
  }

}



