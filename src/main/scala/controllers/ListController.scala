package controllers

import actors.{EventBusImpl, ListCommand, Response, WebSocketActor}
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._

import javax.inject._

class ListController @Inject()(cc: ControllerComponents, eventBus: EventBusImpl)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) with Formatters {

  implicit val messageFlowTransformer: MessageFlowTransformer[ListCommand, Response] = MessageFlowTransformer.jsonMessageFlowTransformer[ListCommand, Response]

  def socket(): WebSocket = WebSocket.accept[ListCommand, Response] { _ =>
    ActorFlow.actorRef { client =>
      WebSocketActor.props(client, eventBus)
    }
  }
}
