package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}

import scala.collection.mutable


case class Subscribe(subscriber: ActorRef)


class EventBus extends Actor with ActorLogging {


  val subscribers: mutable.Set[ActorRef] = mutable.Set.empty

  override def receive: Receive = {
    case msg: ListCreated =>
      subscribers.foreach(_ ! msg)

    case Subscribe(subscriber) =>
      context.watch(subscriber)
      subscribers.add(subscriber)

    case Terminated(subscriber) =>
      subscribers.dropWhile(_ == subscriber)
  }
}

object EventBus {
  def getActorRef(actorSystem: ActorSystem): ActorRef = actorSystem.actorOf(
    ClusterSingletonProxy.props(
      singletonManagerPath = "/user/eventBus",
      settings = ClusterSingletonProxySettings(actorSystem))
  )

  def init(actorSystem: ActorSystem): ActorRef = actorSystem.actorOf(
    ClusterSingletonManager.props(
      singletonProps = Props(new EventBus()),
      terminationMessage = "StopEntity",
      settings = ClusterSingletonManagerSettings(actorSystem)),
    name = "eventBus")
}
