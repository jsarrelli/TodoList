package actors

import akka.actor._
import akka.event.{Logging, LoggingReceive}
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

  override def preStart(): Unit = {
    eventBus.subscribe(self, classOf[ListCreated])
    ElasticSearch.getLists().map(lists => client ! CurrentLists(lists))
  }

  def receive: Receive = LoggingReceive {
    case message: ListCommand =>
      log.info(s"Web socket received message ${message.getClass.getSimpleName}.. forwarding to actor")
      log.info(s"ClientRef: ${sender()}, WebSocketRef: ${self}")
      listRegion ! message

    case listCreated: ListCreated =>
      log.info("New list has been created")
      val listId = listCreated.listId
      listRegion ! GetList(listId)

    case msg: ListState =>
      log.info(s"Respondiendole a este pelotudo desde ${self}")
      client ! msg
  }

  //TODO do we need some validation for non-existing lists?
}