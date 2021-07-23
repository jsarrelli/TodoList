package actors

import akka.actor.ActorRef
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import app.MongoTestEnv
import app.TestApplication.{actorSystem, elasticSearch}
import org.mockito.Mockito.verify
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.util.Random

class WebSocketActorSpec
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

  val listRegion = ListActor.listRegion(actorSystem, elasticSearch)

  def newWebSocketActor(client: ActorRef, listRegion: ActorRef = listRegion): ActorRef =
    system.actorOf(WebSocketActor.props(client, listRegion, elasticSearch))

  def randomId: Long = Random.nextInt(Int.MaxValue).toLong

  "WebSocketActor" should {
    "forward all list commands to the list region" in {
      val mockedListRegion = TestProbe()
      val client = TestProbe()
      val webSocket = newWebSocketActor(client.testActor, mockedListRegion.testActor)

      val command1 = CreateList(123, "New List")
      val command2 = DeleteTask(123, 12)
      val command3 = UpdateTask(213, 123, "Some description", true, 0)
      val command4 = UpdateTaskOrder(12, 23, 2)
      val command5 = GetList(21)

      webSocket ! command1
      mockedListRegion.expectMsg(command1)
      webSocket ! command2
      mockedListRegion.expectMsg(command2)
      webSocket ! command3
      mockedListRegion.expectMsg(command3)
      webSocket ! command4
      mockedListRegion.expectMsg(command4)
      webSocket ! command5
      mockedListRegion.expectMsg(command5)
    }

    "forward a GetList command so it can be replayed to the client" in {
      val client = TestProbe()
      val webSocket = newWebSocketActor(client.testActor)
      client.expectMsgType[CurrentLists]

      webSocket ! GetList(123)
      client.expectMsgType[ListState]
    }

    "notify client when a new list is created" in {
      val client = TestProbe()
      val webSocket = newWebSocketActor(client.testActor)
      client.expectMsgType[CurrentLists]

      webSocket ! ListCreated(randomId, "New List")
      val currentLists1 = client.expectMsgType[CurrentLists]
      currentLists1.lists.size shouldBe 1

      webSocket ! ListCreated(randomId, "New List")
      val currentLists2 = client.expectMsgType[CurrentLists]
      currentLists2.lists.size shouldBe 2

      webSocket ! ListCreated(randomId, "New List")
      val currentLists3 = client.expectMsgType[CurrentLists]
      currentLists3.lists.size shouldBe 3
    }

    "complete flow of list creation" in {
      val client = TestProbe()
      val listId = randomId

      val webSocket = newWebSocketActor(client.testActor)
      client.expectMsgType[CurrentLists]

      webSocket ! CreateList(listId, "New List")
      val currentLists1 = client.expectMsgType[CurrentLists]
      currentLists1.lists.size shouldBe 1

      webSocket ! GetList(listId)
      val listCreated = client.expectMsgType[ListState]
      listCreated.state.listId shouldBe listId
      verify(elasticSearch).indexListId(listId.toString, "New List")
    }

  }

}
