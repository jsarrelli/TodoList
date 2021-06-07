package app

import akka.actor
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent
import akka.cluster.typed.{Cluster, Subscribe}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.complete
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.javadsl.AkkaManagement
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn

object ToDoListApp extends App {

  val actorSystem = ActorSystem[Nothing](Behaviors.setup[Nothing] { context =>
    import akka.actor.typed.scaladsl.adapter._
    implicit val classicSystem: actor.ActorSystem = context.system.toClassic
    implicit val ec = context.system.executionContext

    val cluster = Cluster(context.system)
    context.log.info("Started [" + context.system + "], cluster.selfAddress = " + cluster.selfMember.address + ")")

    Http().newServerAt("0.0.0.0", 8080).bind(complete("ToDoList app started up"))

    // Create an actor that handles cluster domain events
    val listener = context.spawn(Behaviors.receive[ClusterEvent.MemberEvent]((ctx, event) => {
      ctx.log.info("MemberEvent: {}", event)
      Behaviors.same
    }), "listener")

    Cluster(context.system).subscriptions ! Subscribe(listener, classOf[ClusterEvent.MemberEvent])

    AkkaManagement.get(classicSystem).start()
    ClusterBootstrap.get(classicSystem).start()
    Behaviors.empty
  }, "todolist-app")

  implicit val executionContext = actorSystem.executionContext
  implicit val classicSystem: actor.ActorSystem = actorSystem.classicSystem


  val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server online at http://localhost:2551/")

  CoordinatedShutdown(actorSystem)
    .addTask(CoordinatedShutdown.PhaseServiceUnbind, "Stop Akka http") { () =>
      bindingFuture.flatMap(_.unbind()) // trigger unbinding from the port
  }


}
