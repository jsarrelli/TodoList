package api

import actors.{EventBusImpl, ListActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.javadsl.AkkaManagement
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment, Mode}

import scala.jdk.CollectionConverters._
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContextExecutor


object TodoListModule {
  val todolistActorSystemName = "Todolist-Domain"

  class TodoListSystem(val system: ActorSystem)
}

class TodoListModule(environment: Environment, configuration: Configuration) extends Module {

  import TodoListModule._

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    implicit val conf: Config = configuration.get[String]("env") match {
      case "LOCAL" => withSeedNodes(configuration)
      case _ => configuration.underlying
    }
    implicit val actorSystem: ActorSystem = ActorSystem(todolistActorSystemName, conf.getConfig(todolistActorSystemName))
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher
    implicit val materializer: Materializer = Materializer(actorSystem)
    implicit val implicitEnv: Environment = environment


    AkkaManagement.get(actorSystem).start()
    ClusterBootstrap.get(actorSystem).start()

    val eventBusImpl = new EventBusImpl()

    val listRegion: ActorRef = ClusterSharding(actorSystem).start(
      typeName = "List",
      entityProps = ListActor.props(eventBusImpl),
      settings = ClusterShardingSettings(actorSystem),
      extractEntityId = ListActor.extractEntityId,
      extractShardId = ListActor.extractShardId
    )

    bind[ActorRef].qualifiedWith("ListRegion").toInstance(listRegion) ::
      bind[EventBusImpl].toInstance(eventBusImpl) ::
      Nil
  }

  private def withSeedNodes(configuration: Configuration) = {
    ConfigFactory
      .empty()
      .withValue(
        s"$todolistActorSystemName.akka.cluster.seed-nodes",
        ConfigValueFactory.fromIterable(List("akka://Todolist-Domain@192.168.200.7:25520").asJava)
      )
      .withFallback(configuration.underlying)
  }
}


