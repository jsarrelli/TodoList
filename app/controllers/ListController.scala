package controllers

import actors.{Command, ListActor, Response, WebSocketActor}
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._

import javax.inject._


class ListController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {

  //TODO replace it with sharding
  val listActor = system.actorOf(ListActor.props())


  //TODO do better, needs to include its class type

  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[JsValue, JsValue]

  def socket(listId: String): WebSocket = WebSocket.accept[JsValue, JsValue] { _ =>
    ActorFlow.actorRef { client =>
      WebSocketActor.props(listId.toLong, client, listActor)
    }
  }
}
