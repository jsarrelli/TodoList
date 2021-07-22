package api


import actors.EventBusImpl
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.AbstractModule
import controllers.ListController
import net.codingwell.scalaguice.ScalaModule
import play.api.Application
import play.api.libs.concurrent.AkkaGuiceSupport


object TodoListModule {
  val todolistActorSystemName = "Todolist-Domain"

  class TodoListSystem(val system: ActorSystem)
}

class TodoListModule extends AbstractModule with AkkaGuiceSupport with ScalaModule  {

  override def configure(): Unit = {
    bind(classOf[ClusterStart]).asEagerSingleton()
    bind[ListController].asEagerSingleton()
    bind(classOf[EventBusImpl]).asEagerSingleton()
  }



/*private def withSeedNodes(configuration: Configuration) = {
  ConfigFactory
  .empty()
  .withValue(
  s"$todolistActorSystemName.akka.cluster.seed-nodes",
  ConfigValueFactory.fromIterable(List("akka://Todolist-Domain@192.168.200.7:25520").asJava)
  )
  .withFallback(configuration.underlying)*/
}


