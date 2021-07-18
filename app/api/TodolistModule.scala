package api

import actors.ListActor.props
import actors.{EventBusImpl, ListActor}
import akka.actor.{ActorRef, ActorSystem, CoordinatedShutdown}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.Materializer
import com.google.inject.name.Names
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContextExecutor


object TodolistModule {
  val actorSystemName = "Todolist-Domain"

  class TodolistSystem(val system: ActorSystem)
}

class TodolistModule(environment: Environment, configuration: Configuration)
  extends Module {

  import TodolistModule._

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {

    implicit val actorSystem: ActorSystem =
      ActorSystem(actorSystemName,configuration.underlying.getConfig(actorSystemName))
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher
    implicit val materializer: Materializer = Materializer(actorSystem)
    implicit val implicitEnv: Environment = environment
    implicit val conf: Configuration = configuration
    //actorSystem.actorOf(Props[KeepMajoritySplitBrainResolver](), "KeepMajoritySplitBrainResolver")

    CoordinatedShutdown(actorSystem)

    AkkaManagement(actorSystem).start()

    ClusterBootstrap(actorSystem).start()
    val eventBus = new EventBusImpl

    val listRegion = ClusterSharding(actorSystem).start(
      typeName = "List",
      entityProps = props(eventBus),
      settings = ClusterShardingSettings(actorSystem),
      extractEntityId = ListActor.extractEntityId,
      extractShardId = ListActor.extractShardId)


    bind[ActorSystem].qualifiedWith(actorSystemName).toInstance(actorSystem) ::
      bind[TodolistSystem].toInstance(new TodolistSystem(actorSystem)) ::
      bind[EventBusImpl].toInstance(eventBus) ::
      bind[ActorRef].qualifiedWith(Names.named("ListRegion")).toInstance(listRegion) ::
      Nil
  }

}


