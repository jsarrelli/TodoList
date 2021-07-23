package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}

import scala.collection.mutable


class EventBus extends Actor with ActorLogging {

  import EventBus._

  val subscribers: mutable.Map[Class[_ <: Any], Set[ActorRef]] = mutable.Map.empty

  override def receive: Receive = {
    case Subscribe(topic) =>
      val subscriber = sender()
      registerNewSubscriber(topic, subscriber)

    case Terminated(terminatedSubscriber) =>
      removeSubscriber(terminatedSubscriber)

    case msg =>
      notifySubscribers(msg)
  }

  private def registerNewSubscriber(topic: Class[_], subscriber: ActorRef): Unit = {
    context.watch(subscriber)
    val updatedSubscribers = subscribers.getOrElse(topic, Set.empty) + subscriber
    subscribers.put(topic, updatedSubscribers)
  }

  private def removeSubscriber(terminatedSubscriber: ActorRef): Unit = {
    subscribers.foreach { case (topic, topicSubscribers) if topicSubscribers.contains(terminatedSubscriber) =>
      val newTopicSubscribers = topicSubscribers.dropWhile(_ == terminatedSubscriber)
      if (newTopicSubscribers.isEmpty) subscribers.remove(topic)
      else subscribers.put(topic, newTopicSubscribers)
    }
  }

  private def notifySubscribers(msg: Any): Unit = {
    val topic = msg.getClass
    subscribers.getOrElse(topic, Set.empty).foreach { subscriber =>
      subscriber ! msg
    }
  }
}

object EventBus {
  case class Subscribe[_ <: Any](topic: Class[_])

  def getRef(actorSystem: ActorSystem): ActorRef = actorSystem.actorOf(
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
