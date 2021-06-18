package actors

import akka.actor._
import controllers.Formatters
import play.api.libs.json.JsValue

object WebSocketActor {
  def props(listId: Long, client: ActorRef, listRegion: ActorRef): Props = {
    Props(new WebSocketActor(listId, client, listRegion))
  }
}

class WebSocketActor(listId: Long, client: ActorRef, listRegion: ActorRef) extends Actor with ActorLogging with Formatters {

  override def preStart(): Unit = {
    listRegion tell(SyncClient(listId, client), client)
  }

  def receive: Receive = {
    case message: JsValue =>
      val command = jsonToCommand(message)
      log.debug(s"Web socket received message ${command.getClass.getSimpleName}.. forwarding to actor")
      listRegion tell(command, client)
  }
}