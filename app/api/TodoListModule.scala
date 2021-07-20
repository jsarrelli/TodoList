package api

import actors.{EventBusImpl, ListActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.javadsl.AkkaManagement
import akka.stream.Materializer
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import models.TodoList
import net.codingwell.scalaguice.ScalaModule
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContextExecutor


object TodoListModule {
  val catalogActorSystemName = "Todolist-Domain"

  class TodoListSystem(val system: ActorSystem)
}

class TodoListModule(environment: Environment, configuration: Configuration) extends Module {

  import TodoListModule._

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    implicit val actorSystem: ActorSystem =
      ActorSystem(catalogActorSystemName, configuration.underlying.getConfig(catalogActorSystemName))
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher
    implicit val materializer: Materializer = Materializer(actorSystem)
    implicit val implicitEnv: Environment = environment
    implicit val conf: Configuration = configuration

    val eventBusImpl = new EventBusImpl()



    // AkkaManagement.get(actorSystem).start()
    // ClusterBootstrap.get(actorSystem).start()

    //TODO replace it sharding
    /*
        val listRegion: ActorRef = ClusterSharding(actorSystem).start(
          typeName = "List",
          entityProps = ListActor.props(),
          settings = ClusterShardingSettings(actorSystem),
          extractEntityId = ListActor.extractEntityId,
          extractShardId = ListActor.extractShardId
        )
    */
    val listRegion = actorSystem.actorOf(ListActor.props(eventBusImpl))

    bind[ActorRef].qualifiedWith("ListRegion").toInstance(listRegion) ::
      bind[EventBusImpl].toInstance(eventBusImpl) ::
      Nil
  }

}


