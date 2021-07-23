package actors

import akka.actor.{Actor, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import app.TestApplication._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class EventBusSpec
    extends TestKit(actorSystem)
    with AnyWordSpecLike
    with Matchers
    with ImplicitSender {

  "Event Bus" should {
    "notify selected Events" in {
      val eventBus = system.actorOf(Props(new EventBus()))
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val subscribeToListCreated = EventBus.Subscribe(classOf[ListCreated])
      val subscribeToTaskDeleted = EventBus.Subscribe(classOf[TaskDeleted])

      probe1.send(eventBus, subscribeToListCreated)

      eventBus ! ListCreated(123, "Some List")
      eventBus ! TaskDeleted(222)

      probe1.expectMsgType[ListCreated](10 seconds)
      probe1.expectNoMessage()

      probe2.send(eventBus, subscribeToTaskDeleted)
      eventBus ! ListCreated(123, "Some List")
      eventBus ! TaskDeleted(222)

      probe2.expectMsgType[TaskDeleted]
      probe2.expectNoMessage()

    }

    "remove subscriber if terminated" in {
      val eventBus = system.actorOf(Props(new EventBus()))
      val probe = TestProbe()
      case class SomeEvent()

      case class Subscriber() extends Actor {
        override def receive: Receive = { case msg =>
          probe.testActor ! msg
        }
      }
      val subscriber = system.actorOf(Props(Subscriber()), "Subscriber")

      watch(subscriber)
      eventBus tell (EventBus.Subscribe(classOf[SomeEvent]), subscriber)
      eventBus ! SomeEvent()
      probe.expectMsgType[SomeEvent]

      system.stop(subscriber)
      expectTerminated(subscriber)

      system.actorOf(Props(Subscriber()), "Subscriber")
      eventBus ! SomeEvent()
      probe.expectNoMessage()
    }

  }

}
