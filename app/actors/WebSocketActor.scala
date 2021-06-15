package actors

import akka.actor._

object WebSocketActor {
  def props(client: ActorRef, listRegion: ActorRef): Props = {
    Props(new WebSocketActor(client, listRegion))
  }
}


class WebSocketActor(client: ActorRef, listRegion: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {
    case msg =>
      log.info("El web socket lo recibio")
      listRegion tell(msg, client)
  }
}