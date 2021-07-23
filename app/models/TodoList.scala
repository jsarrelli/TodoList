package models

object TodoList {

  def apply(listId: Long, name: String): TodoList = {
    new TodoList(listId, name, List.empty)
  }

  def emptyList(): TodoList = TodoList(-1, "")
}

case class TodoList(listId: Long, name: String, tasks: List[Task]) {

  def newTask(taskId: Long, description: String): TodoList = {
    val newTask = Task(taskId = taskId, completed = false, description = description, order = 999)
    copy(tasks = tasks :+ newTask).reorderTasks
  }

  def removeTask(taskId: Long): TodoList = {
    copy(tasks = tasks.filter(_.taskId != taskId)).reorderTasks
  }

  def updateTask(taskId: Long, newDescription: String, completed: Boolean, order: Int): TodoList = {
    val updatedTasks = tasks.collect {
      case task if task.taskId == taskId =>
        task.copy(description = newDescription, completed = completed)
      case task => task
    }
    copy(tasks = updatedTasks).updateTaskOrder(taskId, order)
  }

  def updateTaskOrder(taskId: Long, order: Int): TodoList = {
    val (head, tail) = tasks.filterNot(_.taskId == taskId).splitAt(order)
    val taskToReorder = tasks.find(_.taskId == taskId).toList
    val updatedTasks = head ++ taskToReorder ++ tail
    copy(tasks = updatedTasks).reorderTasks
  }

  private def reorderTasks: TodoList = {
    val updatedTasks = tasks.zipWithIndex.foldLeft(List.empty[Task]) { case (list, (task, index)) =>
      list :+ task.copy(order = index)
    }
    copy(tasks = updatedTasks)
  }

  def completeTask(taskId: Long): TodoList = {
    copy(tasks = tasks.map {
      case x if x.taskId == taskId => x.complete()
      case x                       => x
    })
  }
}
