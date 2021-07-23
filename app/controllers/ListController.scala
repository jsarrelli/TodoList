package controllers

import actors.{ListActor, ListCommand, Response, WebSocketActor}
import akka.actor.ActorSystem
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._

import javax.inject._

class ListController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) with Formatters {

  implicit val messageFlowTransformer: MessageFlowTransformer[ListCommand, Response] = MessageFlowTransformer.jsonMessageFlowTransformer[ListCommand, Response]

  val listRegion = ListActor.listRegion(actorSystem)

  def socket(): WebSocket = WebSocket.accept[ListCommand, Response] { _ =>
    ActorFlow.actorRef { client =>
      WebSocketActor.props(client, listRegion)
    }
  }
}
