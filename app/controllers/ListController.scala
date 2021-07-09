package controllers

import actors.{EventBusImpl, ListCommand, WebSocketActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._

import javax.inject._

class ListController @Inject()(cc: ControllerComponents, @Named("ListRegion") listRegion: ActorRef, eventBus: EventBusImpl)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) with Formatters {

  implicit val messageFlowTransformer: MessageFlowTransformer[ListCommand, JsValue] = MessageFlowTransformer.jsonMessageFlowTransformer[ListCommand, JsValue]

  def socket(): WebSocket = WebSocket.accept[ListCommand, JsValue] { _ =>
    ActorFlow.actorRef { client =>
      WebSocketActor.props(client, listRegion, eventBus)
    }
  }
}
