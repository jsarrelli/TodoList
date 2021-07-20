package actors

import akka.actor._
import akka.event.Logging
import api.ElasticSearch
import models.{ListDescription, TodoList}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait Response

final case class ListState(state: TodoList) extends Response

final case class CurrentLists(lists: List[ListDescription]) extends Response

object WebSocketActor {
  def props(client: ActorRef, listRegion: ActorRef, eventBusImpl: EventBusImpl): Props = {
    Props(new WebSocketActor(client, listRegion, eventBusImpl))
  }
}

class WebSocketActor(client: ActorRef, listRegion: ActorRef, eventBus: EventBusImpl) extends Actor with ActorLogging {

  val logger = Logging(context.system, this)

  override def preStart(): Unit = {
    eventBus.subscribe(self, classOf[ListCreated])
    ElasticSearch.getLists().map(lists => client ! CurrentLists(lists))
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