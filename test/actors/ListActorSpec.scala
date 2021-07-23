package actors

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion.Passivate
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import app.MongoTestEnv
import app.TestApplication._
import models.TodoList
import org.mockito.Mockito.{times, verify}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.postfixOps
import scala.util.Random

class ListActorSpec
    extends TestKit(actorSystem)
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with MongoTestEnv {

  override def afterAll(): Unit = {
    cleanCollections()
    elasticSearch.deleteListIndex()
  }

  case class ListActorTest(override val listId: String) extends ListActor(elasticSearch)

  def newActor(listId: String): ActorRef = system.actorOf(Props(ListActorTest(listId)))

  def randomId: Long = Random.nextInt(Int.MaxValue).toLong

  "CreateList" should {
    "create a list, set state and persist it" in {
      val listId = randomId
      val list = newActor(listId.toString)

      list ! CreateList(listId, "New List")
      list ! GetList(listId)
      expectMsgType[ListState].state shouldBe TodoList(listId, "New List")

      watch(list)
      list ! Passivate
      expectTerminated(list)

      val recoveredList = newActor(listId.toString)
      recoveredList ! GetList(listId)
      expectMsgType[ListState].state shouldBe TodoList(listId, "New List")
    }

    "not recreate a list if it was previously created" in {
      val listId = randomId
      val list = newActor(listId.toString)
      list ! CreateList(listId, "First List")
      list ! CreateList(listId, "Another List")
      list ! GetList(listId)
      val listState = expectMsgType[ListState]
      listState.state shouldBe TodoList(listId, "First List")
    }
    "not respond any message" in {
      val listId = randomId
      val list = newActor(listId.toString)
      list ! CreateList(listId, "New List")
      expectNoMessage()
    }
  }

  "CreateTask" should {
    "create a task, update state and persist it" in {
      val listId = randomId
      val list = newActor(listId.toString)
      list ! CreateList(listId, "New List")
      list ! CreateTask(listId, 2, "New task")
      list ! GetList(listId)
      val listState = expectMsgType[ListState]
      listState.state.tasks.head.description shouldBe "New task"
      listState.state.tasks.size shouldBe 1

      watch(list)
      list ! Passivate
      expectTerminated(list)

      val recoveredList = newActor(listId.toString)
      recoveredList ! GetList(listId)
      val recoveredState = expectMsgType[ListState]
      recoveredState.state.tasks.head.description shouldBe "New task"
      recoveredState.state.tasks.size shouldBe 1
    }
  }

  "DeleteTask" should {
    "delete a task, update state and persist it" in {
      val listId = randomId
      val list = newActor(listId.toString)
      list ! CreateList(listId, "New List")
      list ! GetList(listId)
      expectMsgType[ListState].state.tasks.size shouldBe 0

      list ! CreateTask(listId, 2, "New task")
      list ! GetList(listId)
      expectMsgType[ListState].state.tasks.size shouldBe 1

      list ! DeleteTask(listId, 2)
      list ! GetList(listId)
      expectMsgType[ListState].state.tasks.size shouldBe 0

      watch(list)
      list ! Passivate
      expectTerminated(list)

      val recoveredList = newActor(listId.toString)
      recoveredList ! GetList(listId)
      expectMsgType[ListState].state.tasks.size shouldBe 0
    }
  }

  "EventBus" should {
    "be notified for a ListCreated event" in {
      val eventBus = EventBus.getRef(actorSystem)
      val testProbe = TestProbe()
      testProbe.send(eventBus, EventBus.Subscribe(classOf[ListCreated]))
      val listId = randomId
      val list = newActor(listId.toString)
      list ! CreateList(listId, "New List")
      testProbe.expectMsgType[ListCreated].listId shouldBe listId
    }
  }

  "ElasticSearch" should {
    "be notified for a ListCreated event" in {
      val listId = randomId
      val list = newActor(listId.toString)
      list ! CreateList(listId, "New List")
      list ! GetList(listId)
      expectMsgType[ListState]
      verify(elasticSearch, times(1)).indexListId(listId.toString, "New List")
    }
  }

  "ClusterSharding" should {
    "redirect messages to its actor" in {
      val listRegion = ListActor.listRegion(actorSystem, elasticSearch)
      val listIdA = randomId
      val listIdB = randomId
      val listIdC = randomId

      listRegion ! CreateList(listIdA, "ListA")
      listRegion ! CreateList(listIdB, "ListB")
      listRegion ! CreateList(listIdC, "ListC")

      listRegion ! GetList(listIdA)
      expectMsgType[ListState].state.name shouldBe "ListA"

      listRegion ! GetList(listIdB)
      expectMsgType[ListState].state.name shouldBe "ListB"

      listRegion ! GetList(listIdB)
      expectMsgType[ListState].state.name shouldBe "ListB"
    }

  }

}
