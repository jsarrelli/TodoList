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
  def props(client: ActorRef, listRegion: ActorRef): Props = {
    Props(new WebSocketActor(client, listRegion))
  }
}

class WebSocketActor(client: ActorRef, listRegion: ActorRef) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    val eventBus = EventBus.getActorRef(context.system)
    eventBus ! Subscribe(self)
    ElasticSearch.getLists().foreach(lists => client ! CurrentLists(lists))
  }

  def receive: Receive = {
    case message: ListCommand =>
      listRegion tell (message,client)

    case listCreated: ListCreated =>
      val listId = listCreated.listId
      listRegion tell(GetList(listId), client)
  }

  //TODO do we need some validation for non-existing lists?
}