package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import akka.event.{Logging, LoggingReceive}
import controllers.Formatters
import models.{Task, TodoList}

import javax.inject.Inject

sealed trait ListCommand {
  val listId: Long
}

final case class CreateList(listId: Long, name: String) extends ListCommand

final case class CreateTask(listId: Long, taskId: Long, description: String) extends ListCommand

final case class UpdateTask(listId: Long, taskId: Long, description: String, completed: Boolean, order: Int) extends ListCommand

final case class UpdateTaskOrder(listId: Long, taskId: Long, order: Int) extends ListCommand

final case class DeleteTask(listId: Long, taskId: Long) extends ListCommand

final case class GetList(listId: Long) extends ListCommand

trait ListEvent

final case class ListCreated(list: TodoList) extends ListEvent

final case class TaskCreated(task: Task) extends ListEvent

final case class TaskRemoved(taskId: Long) extends ListEvent

final case class TaskCompleted(taskId: Long) extends ListEvent

final case class TaskUpdated(task: Task) extends ListEvent


class ListActor @Inject()(eventBus: EventBusImpl) extends Actor with ActorLogging with Formatters {

  val logger = Logging(context.system, this)

  val id: String = self.path.name
  logger.info(s"Actor created: $id")

  var state: TodoList = TodoList.emptyList()
  var taskIds: Long = 0

  override def receive: Receive = {

    case CreateList(listId, name) =>
      state = TodoList(listId, name)
      updateState(state)
      val event = ListCreated(state)
      eventBus.publish(event)

    case CreateTask(_, taskId, description) =>
      val newState = state.newTask(taskId, description)
      updateState(newState)

    case UpdateTask(_, taskId, description, completed, order) =>
      val newState = state.updateTask(taskId, description, completed, order)
      updateState(newState)

    case UpdateTaskOrder(_, taskId, order) =>
      val newState = state.updateTaskOrder(taskId, order)
      updateState(newState)

    case DeleteTask(_, taskId) =>
      val newState = state.removeTask(taskId)
      updateState(newState)

    case _: GetList =>
      println("recibi algoo")
      val client = sender()
      sendState(client)
  }

  def sendState(client: ActorRef): Unit = client ! ListState(state)

  def updateState(newState: TodoList): Unit = this.state = newState
}

object ListActor {
  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg: ListCommand => (msg.listId.toString, msg)
  }

  val numberOfShards = 100

  val extractShardId: ShardRegion.ExtractShardId = {
    case msg: ListCommand => (msg.listId % numberOfShards).toString
    case ShardRegion.StartEntity(id) =>
      // StartEntity is used by remembering entities feature
      (id.toLong % numberOfShards).toString
    case _ => throw new IllegalArgumentException()
  }

  def props(eventBus: EventBusImpl): Props = Props(new ListActor(eventBus))

}