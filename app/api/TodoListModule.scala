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
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.concurrent.ExecutionContextExecutor


class TodoListModule extends AbstractModule with AkkaGuiceSupport with ScalaModule {


  override def configure(): Unit = {
    /*
        implicit val actorSystem: ActorSystem = ActorSystem("todolist-app")
        implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher
        implicit val materializer: Materializer = Materializer(actorSystem)
    */


    //Cluster Sharding

    /*
        val listRegion: ActorRef = ClusterSharding(actorSystem).start(
          typeName = "List",
          entityProps = ListActor.props(),
          settings = ClusterShardingSettings(actorSystem),
          extractEntityId = ListActor.extractEntityId,
          extractShardId = ListActor.extractShardId
        )
    */
    val eventBus = new EventBusImpl

    //bind(classOf[ActorSystem]).annotatedWith(Names.named("TodoListSystem")).toInstance(actorSystem)
    bindActor[ListActor]("ListRegion")
    bind(classOf[EventBusImpl]).asEagerSingleton()


    // AkkaManagement.get(actorSystem).start()
    // ClusterBootstrap.get(actorSystem).start()
  }


}


