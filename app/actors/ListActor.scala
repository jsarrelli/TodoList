package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import controllers.Formatters
import models.{Task, TodoList}
import play.api.libs.json.JsValue

import scala.collection.mutable
import scala.util.Try


sealed trait Command {
  val listId: Long
}

final case class CreateTask(listId: Long, description: String) extends Command

final case class RemoveTask(listId: Long, taskId: Long) extends Command

final case class CompleteTask(listId: Long, taskId: Long) extends Command

final case class UpdateTask(listId: Long, taskId: Long, description: String) extends Command

final case class GetList(listId: Long) extends Command

final case class SyncClient(listId: Long, client: ActorRef)


sealed trait Response

final case class TaskCreated(task: Task) extends Response

final case class TaskRemoved(taskId: Long) extends Response

final case class TaskCompleted(taskId: Long) extends Response

final case class TaskUpdated(task: Task) extends Response

final case class State(list: TodoList) extends Response


class ListActor extends Actor with ActorLogging with Formatters {

  val clients = mutable.Set.empty[ActorRef]
  val id: String = self.path.name
  log.info(s"Name es $id")
  var state: TodoList = TodoList(12, "Bufa List", List.empty[Task])
  var taskIds: Long = 0

  override def receive: Receive = {
    case CreateTask(_, description) =>
      val (newState, newTask) = state.addTask(description, taskIds)
      val event = TaskCreated(newTask)
      taskIds = taskIds + 1
      updateState(newState)
      notifyClients(event)

    case RemoveTask(_, taskId) =>
      val newState = state.removeTask(taskId)
      val event = TaskRemoved(taskId)
      updateState(newState)
      notifyClients(event)

    case CompleteTask(_, taskId) =>
      val newState = state.completeTask(taskId)
      val event = TaskCompleted(taskId)
      updateState(newState)
      notifyClients(event)
    case UpdateTask(_,taskId,description) =>Try{
      val (newState,updateTask) = state.updateTask(taskId,description)
      val event =  TaskUpdated(updateTask)
      updateState(newState)
      notifyClients(event)
    } recover{
      case ex => log.error("Failed",ex)
        //TODO notify client
    }
    case _: GetList =>
      val client = sender()
      sendState(client)
    case SyncClient(listID, client) =>
      log.info(s"Id es :$listID")
      //TODO not working properly, repeating same client
      clients.add(client)
      sendState(client)
  }

  def notifyClients(event: Response): Unit = {
    clients.foreach(_ ! responseToJson(event))
  }

  def sendState(client: ActorRef): Unit = client ! responseToJson(State(state))

  def updateState(newState: TodoList): Unit = this.state = newState
}


object ListActor {
  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg: Command => (msg.listId.toString, msg)
  }

  val numberOfShards = 100

  val extractShardId: ShardRegion.ExtractShardId = {
    case msg: Command => (msg.listId % numberOfShards).toString
    case ShardRegion.StartEntity(id) =>
      // StartEntity is used by remembering entities feature
      (id.toLong % numberOfShards).toString
    case _ => throw new IllegalArgumentException()
  }


  def props(): Props = Props(new ListActor())

}