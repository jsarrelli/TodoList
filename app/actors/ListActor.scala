package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import akka.event.Logging
import akka.persistence.{PersistentActor, SnapshotOffer}
import models.TodoList

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

trait ListEvent {
  def applyTo(state: TodoList): TodoList
}

final case class ListCreated(listId: Long, name: String) extends ListEvent {
  override def applyTo(state: TodoList): TodoList = TodoList(listId, name)
}

final case class TaskCreated(taskId: Long, description: String) extends ListEvent {
  override def applyTo(state: TodoList): TodoList = state.newTask(taskId, description)
}

final case class TaskDeleted(taskId: Long) extends ListEvent {
  override def applyTo(state: TodoList): TodoList = state.removeTask(taskId)
}

final case class TaskUpdated(taskId: Long, description: String, completed: Boolean, order: Int) extends ListEvent {
  override def applyTo(state: TodoList): TodoList = state.updateTask(taskId, description, completed, order)
}

final case class TaskOrderUpdated(taskId: Long, order: Int) extends ListEvent {
  override def applyTo(state: TodoList): TodoList = state.updateTaskOrder(taskId, order)
}

class ListActor @Inject()(eventBus: EventBusImpl) extends Actor with PersistentActor with ActorLogging  {

  val logger = Logging(context.system, this)

  logger.info(s"Actor created: $listId")

  val listId: String = self.path.name

  override def persistenceId: String = s"List-$listId"

  var state: TodoList = TodoList.emptyList()

  override def receiveCommand: Receive = {

    case CreateList(listId, name) =>
      //TODO weird, could potentially lead to multiple creations of the same list entity
      val event = ListCreated(listId, name)
      persistAndUpdateState(event)

    case CreateTask(_, taskId, description) =>
      val event = TaskCreated(taskId, description)
      persistAndUpdateState(event)

    case UpdateTask(_, taskId, description, completed, order) =>
      val event = TaskUpdated(taskId, description, completed, order)
      persistAndUpdateState(event)

    case UpdateTaskOrder(_, taskId, order) =>
      val event = TaskOrderUpdated(taskId, order)
      persistAndUpdateState(event)

    case DeleteTask(_, taskId) =>
      val event = TaskDeleted(taskId)
      persistAndUpdateState(event)

    case _: GetList =>
      val client = sender()
      sendState(client)
  }

  def sendState(client: ActorRef): Unit = client ! ListState(state)

  def updateState(newState: TodoList): Unit = this.state = newState

  def persistAndUpdateState(event: ListEvent): Unit = persist(event) { _ =>
    val newState = event.applyTo(state)
    updateState(newState)


    if (event.isInstanceOf[ListCreated]) eventBus.publish(event)
  }

  def saveSnapshot(): Unit = {
    val snapshotInterval = 10
    if (lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0)
      saveSnapshot(state)
  }

  override def receiveRecover: Receive = {

    case SnapshotOffer(_, snapshot: TodoList) =>
      updateState(snapshot)
    case event: ListEvent =>
      val newState = event.applyTo(state)
      updateState(newState)
  }
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