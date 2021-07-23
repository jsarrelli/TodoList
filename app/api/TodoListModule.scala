package api

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import controllers.ListController
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

object TodoListModule {
  val todolistActorSystemName = "Todolist-Domain"

  class TodoListSystem(val system: ActorSystem)
}

class TodoListModule extends AbstractModule with AkkaGuiceSupport with ScalaModule {

  override def configure(): Unit = {
    bind(classOf[ClusterStart]).asEagerSingleton()
    bind[ListController].asEagerSingleton()
    bind[ElasticSearchApi]
  }
}
