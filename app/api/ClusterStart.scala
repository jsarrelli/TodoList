package api

import actors.EventBus
import akka.actor.ActorSystem
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.Materializer
import play.api.mvc.{AbstractController, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContextExecutor

@Singleton
class ClusterStart @Inject() (system: ActorSystem, cc: ControllerComponents)
    extends AbstractController(cc) {
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = Materializer(system)

  AkkaManagement.get(system).start()
  ClusterBootstrap.get(system).start()

  EventBus.init(system)
}
