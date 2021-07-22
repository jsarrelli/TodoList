package controllers

import actors.{EventBusImpl, ListActor, ListCommand, Response, WebSocketActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.stream.Materializer
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._

import javax.inject._

class ListController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem, eventBus: EventBusImpl)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) with Formatters {

  implicit val messageFlowTransformer: MessageFlowTransformer[ListCommand, Response] = MessageFlowTransformer.jsonMessageFlowTransformer[ListCommand, Response]

  val listRegion = ClusterSharding(actorSystem).start(
    typeName = "List",
    entityProps = ListActor.props(eventBus),
    settings = ClusterShardingSettings(actorSystem),
    extractEntityId = ListActor.extractEntityId,
    extractShardId = ListActor.extractShardId
  )

  def socket(): WebSocket = WebSocket.accept[ListCommand, Response] { _ =>
    ActorFlow.actorRef { client =>
      WebSocketActor.props(client, listRegion, eventBus)
    }
  }
}
