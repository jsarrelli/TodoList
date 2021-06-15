package models

case class Task(taskId: Long, completed: Boolean, description: String){

  def complete(): Task = copy(completed = true)
}
