package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import models.{Task, TodoList}

import scala.collection.mutable


sealed trait Command {
  val listId: Long
}

final case class CreateTask(listId: Long, description: String) extends Command

final case class RemoveTask(listId: Long, taskId: Long) extends Command

final case class CompleteTask(listId: Long, taskId: Long) extends Command

final case class UpdateTask(listId: Long, taskId: Long, description: String) extends Command

final case class GetList(listId: Long) extends Command

final case class SyncClient(listId: Long, client: ActorRef) extends Command


sealed trait Event

final case class TaskCreated(task: Task) extends Event

final case class TaskRemoved(taskId: Long) extends Event

final case class TaskCompleted(taskId: Long) extends Event

final case class TaskUpdated(task: Task) extends Event

final case class State(list: TodoList) extends Event


class ListActor extends Actor with ActorLogging {

  val clients = mutable.Set.empty[ActorRef]
  val id: String = self.path.name
  var state: TodoList = TodoList(id.toLong, "List", List.empty[Task])
  var taskIds: Long = 0

  override def receive: Receive = command => {
    val client = sender()
    clients add client

    command match {
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

      case _: GetList =>
        val client = sender()
        client ! State(state)
    }
  }

  def notifyClients(event: Event): Unit = {
    clients.foreach(_ ! event)
  }

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