package actors

import akka.actor.ActorRef
import akka.event.{EventBus, LookupClassification}

import javax.inject.Singleton

@Singleton
class EventBusImpl extends EventBus with LookupClassification {
  override type Event = ListEvent
  override type Classifier = Class[_ <:ListEvent]
  override type Subscriber = ActorRef

  override protected def mapSize(): Int = 128

  override protected def compareSubscribers(a: ActorRef, b: ActorRef): Int = a.compareTo(b)

  override protected def classify(event: ListEvent): Classifier = event.getClass

  override protected def publish(event: ListEvent, subscriber: ActorRef): Unit = subscriber ! event
}
