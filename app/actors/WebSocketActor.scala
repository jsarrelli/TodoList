package actors

import akka.actor._

object WebSocketActor {
  def props(client: ActorRef, listRegion: ActorRef, eventBusImpl: EventBusImpl): Props = {
    Props(new WebSocketActor(client, listRegion, eventBusImpl))
  }
}

class WebSocketActor(client: ActorRef, listRegion: ActorRef, eventBus: EventBusImpl) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    eventBus.subscribe(self, classOf[ListCreated])
  }

  def receive: Receive = {
    case message: ListCommand =>
      log.debug(s"Web socket received message ${message.getClass.getSimpleName}.. forwarding to actor")
      listRegion forward message

    case listCreated: ListCreated =>
      log.debug("New list has been created")
      val listId = listCreated.list.listId
      listRegion.tell(GetList(listId), client)
  }
}