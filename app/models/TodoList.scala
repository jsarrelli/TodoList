package models

case class TodoList(listId: Long, name: String, tasks: List[Task]) {

  def addTask(description: String, taskId: Long): (TodoList, Task) = {
    val newTask = Task(taskId = taskId, completed = false, description = description)
    (copy(tasks = tasks :+ newTask), newTask)
  }

  def removeTask(taskId: Long): TodoList = {
    copy(tasks = tasks.dropWhile(_.taskId == taskId))
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