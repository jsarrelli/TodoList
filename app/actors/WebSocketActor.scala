package actors

import akka.actor._
import akka.event.Logging
import models.TodoList

sealed trait Response

final case class ListState(state: TodoList) extends Response

object WebSocketActor {
  def props(client: ActorRef, listRegion: ActorRef, eventBusImpl: EventBusImpl): Props = {
    Props(new WebSocketActor(client, listRegion, eventBusImpl))
  }
}

class WebSocketActor(client: ActorRef, listRegion: ActorRef, eventBus: EventBusImpl) extends Actor with ActorLogging {

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
      val listId = listCreated.list.listId
      listRegion.tell(GetList(listId), client)
  }
}