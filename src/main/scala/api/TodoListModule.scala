package api

import actors.{EventBusImpl, ListActor}
import akka.actor.ActorRef
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport


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

    //bind(classOf[ActorSystem]).annotatedWith(Names.named("TodoListSystem")).toInstance(actorSystem)
    bind(classOf[EventBusImpl]).asEagerSingleton()
    bind(classOf[Application]).asEagerSingleton()


    // AkkaManagement.get(actorSystem).start()
    // ClusterBootstrap.get(actorSystem).start()
  }


}


