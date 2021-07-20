package api

import actors.{EventBusImpl, ListActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Directives.{complete, path}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.io.StdIn


object TodoListModule extends App {

  implicit val system = ActorSystem("TodolistDomain")
  implicit val executionContext = system.dispatcher
  val cluster = Cluster(system)
  system.log.info("Started [" + system + "], cluster.selfAddress = " + cluster.selfMember.address + ")")
  system.log.info(system.name)

  // Akka Management hosts the HTTP routes used by bootstrap
  AkkaManagement(system).start()

  // Starting the bootstrap process needs to be done explicitly
  ClusterBootstrap(system).start()

  val route =
    path("hello") {
      Directives.get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}


