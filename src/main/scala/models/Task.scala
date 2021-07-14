package models

case class Task(taskId: Long, completed: Boolean, description: String, order: Int) {

  def complete(): Task = copy(completed = true)
}
