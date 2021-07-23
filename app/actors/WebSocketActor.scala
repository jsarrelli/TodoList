package actors

import akka.actor._
import akka.event.{Logging, LoggingReceive}
import akka.pattern.ask
import akka.util.Timeout
import anorm.Macro.Placeholder.Parser.?
import api.ElasticSearch
import models.{ListDescription, TodoList}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

sealed trait Response

final case class ListState(state: TodoList) extends Response

final case class CurrentLists(lists: List[ListDescription]) extends Response

object WebSocketActor {
  def props(client: ActorRef, listRegion: ActorRef): Props = {
    Props(new WebSocketActor(client, listRegion))
  }
}

class WebSocketActor(client: ActorRef, listRegion: ActorRef) extends Actor with ActorLogging {
  implicit val timeout: Timeout = Timeout(10 seconds)
  val eventBus: ActorRef = EventBus.getRef(context.system)

  override def preStart(): Unit = {
    eventBus ! EventBus.Subscribe(classOf[ListCreated])
    notifyCurrentLists()
  }

  def receive: Receive = {
    case message: ListCommand =>
      listRegion tell(message, client)

    case listCreated: ListCreated =>
      val currentLists = ElasticSearch.getLists().map(_ :+ ListDescription(listCreated.listId.toString, listCreated.name))
      //workaround to avoid elastic search refresh time
      currentLists.foreach(lists => client ! CurrentLists(lists))
  }
  //TODO do we need some validation for non-existing lists?

  private def notifyCurrentLists(): Unit = ElasticSearch.getLists().foreach(lists => client ! CurrentLists(lists))

}