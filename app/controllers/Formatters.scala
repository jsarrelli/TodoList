package controllers

import actors._
import models.{Task, TodoList}
import play.api.libs.json._
import play.api.libs.json.Reads._


trait Formatters {

  //Models
  implicit val taskFormat = Json.format[Task]
  implicit val listFormat = Json.format[TodoList]


  def responseToJson: Response => JsValue = {
    case TaskCreated(task) => Json.obj("response_type" -> "task_created", "task" -> task)
    case TaskRemoved(taskId) => Json.obj("response_type" -> "task_removed", "task" -> taskId)
    case TaskCompleted(taskId) => Json.obj("response_type" -> "task_completed", "task" -> taskId)
    case TaskUpdated(task) => Json.obj("response_type" -> "task_updated", "task" -> task)
    case State(list) => Json.obj("state" -> list)
  }

  def jsonToCommand: JsValue => Command = { json =>
    val listId = (json \ "list_id").as[Long]
    (json \ "command_type").as[String] match {
      case "create_task" =>
        val description = (json \ "description").as[String]
        CreateTask(listId, description)
      case "remove_task" =>
        val taskId = (json \ "task_id").as[Long]
        RemoveTask(listId, taskId)
      case "complete_task" =>
        val taskId = (json \ "task_id").as[Long]
        CompleteTask(listId, taskId)
      case "update_task" =>
        val taskId = (json \ "task_id").as[Long]
        val description = (json \ "description").as[String]
        UpdateTask(listId, taskId, description)
      case "get_list" =>
        GetList(listId)
    }
  }

}