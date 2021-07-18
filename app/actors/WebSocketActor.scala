package actors

import actors.ListActor.props
import akka.actor._
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.event.Logging
import models.TodoList

sealed trait Response

final case class ListState(state: TodoList) extends Response

object WebSocketActor {
  def props(client: ActorRef, eventBusImpl: EventBusImpl,listRegion:ActorRef): Props = {
    Props(new WebSocketActor(client, eventBusImpl,listRegion))
  }
}

class WebSocketActor(client: ActorRef, eventBus: EventBusImpl, listRegion:ActorRef) extends Actor with ActorLogging {



  val logger = Logging(context.system, this)

  override def preStart(): Unit = {
    eventBus.subscribe(self, classOf[ListCreated])
  }

  def receive: Receive = {
    case message: ListCommand =>
      logger.debug(s"Web socket received message ${message.getClass.getSimpleName}.. forwarding to actor")
      listRegion.tell(message, client)

    case listCreated: ListCreated =>
      logger.debug("New list has been created")
      val listId = listCreated.listId
      listRegion.tell(GetList(listId), client)
  }
}