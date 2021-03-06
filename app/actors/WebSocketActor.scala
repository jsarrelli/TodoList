package actors

import akka.actor._
import akka.util.Timeout
import api.ElasticSearchApi
import models.{ListDescription, TodoList}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

sealed trait Response

final case class ListState(state: TodoList) extends Response

final case class CurrentLists(lists: List[ListDescription]) extends Response

object WebSocketActor {

  def props(client: ActorRef, listRegion: ActorRef, elasticSearch: ElasticSearchApi): Props = {
    Props(new WebSocketActor(client, listRegion, elasticSearch))
  }
}

class WebSocketActor(client: ActorRef, listRegion: ActorRef, elasticSearch: ElasticSearchApi)
    extends Actor
    with ActorLogging {
  implicit val timeout: Timeout = Timeout(10 seconds)
  val eventBus: ActorRef = EventBus.getRef(context.system)
  val currentLists = mutable.Set.empty[ListDescription]

  override def preStart(): Unit = {
    eventBus ! EventBus.Subscribe(classOf[ListCreated])
    loadCurrentLists().foreach(_ => notifyClientCurrentLists())
  }

  def receive: Receive = {
    case message: ListCommand =>
      listRegion tell (message, client)

    case listCreated: ListCreated =>
      currentLists.add(ListDescription(listCreated.listId.toString, listCreated.name))
      notifyClientCurrentLists()
  }

  private def loadCurrentLists(): Future[Unit] =
    elasticSearch.getLists().map(_.foreach(currentLists.add))

  private def notifyClientCurrentLists(): Unit =
    client ! CurrentLists(currentLists.toList)
}
