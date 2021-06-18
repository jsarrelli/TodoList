package models

case class TodoList(listId: Long, name: String, tasks: List[Task]) {

  def addTask(description: String, taskId: Long): (TodoList, Task) = {
    val newTask = Task(taskId = taskId, completed = false, description = description)
    (copy(tasks = tasks :+ newTask), newTask)
  }

  def removeTask(taskId: Long): TodoList = {
    copy(tasks = tasks.filter(_.taskId != taskId))
  }

  def updateTask(taskId: Long, newDescription: String): (TodoList, Task) = {
    val updatedTask = tasks.find(_.taskId == taskId)
      .getOrElse(throw new Exception(s"TaskId:$taskId not found"))
      .copy(description = newDescription)

    val updatedTasks = tasks.collect {
      case task if task.taskId == taskId => updatedTask
      case task => task
    }
    (copy(tasks = updatedTasks),updatedTask)
  }

  def completeTask(taskId: Long): TodoList = {
    copy(tasks =
      tasks.map {
        case x if x.taskId == taskId => x.complete()
        case x => x
      }
    )
  }
}