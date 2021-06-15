package controllers

import actors.{Command, Event, ListActor, WebSocketActor}
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.Json
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._

import javax.inject._


class ListController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {

  //TODO replace it with sharding
  val listActor = system.actorOf(ListActor.props())


  //TODO do better, needs to include its class type
  implicit val commandFormat  = Json.format[Command]
  implicit val eventFormat = Json.format[Event]
  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[Command, Event]

  def socket(): WebSocket = WebSocket.accept[Command, Event] { _ =>
    ActorFlow.actorRef { out =>
      WebSocketActor.props(out, listActor)
    }
  }
}
