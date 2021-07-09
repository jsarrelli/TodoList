package controllers

import actors._
import models.{Task, TodoList}
import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json._


trait Formatters {

  implicit val jsonConfiguration: Aux[Json.MacroOptions] = JsonConfiguration(
    discriminator = "type",
    typeNaming = CommandsNaming
  )

  object CommandsNaming extends JsonNaming {
    override def apply(property: String): String = {
      (for (char <- property.split("\\.").last) yield
        if (char.isUpper) "_" + char else "" + char)
        .reduce(_ ++ _)
        .drop(1)
        .toUpperCase
    }
  }
  //Models
  implicit val taskFormat = Json.format[Task]
  implicit val listFormat = Json.format[TodoList]

  implicit val createListFormat = Json.format[CreateList]
  implicit val createTaskFormat = Json.format[CreateTask]
  implicit val removeTaskFormat = Json.format[DeleteTask]
  implicit val updateTaskFormat = Json.format[UpdateTask]
  implicit val updateTaskOrderFormat = Json.format[UpdateTaskOrder]
  implicit val getListFormat = Json.format[GetList]
  implicit val listCommandFormat = Json.format[ListCommand]

  //implicit val getAllListsFormat = ((JsPath \ "type").read[String])(GetAllLists(_))


/*  def responseToJson: Response => JsValue = {
    case TaskCreated(task) => Json.obj("response_type" -> "task_created", "task" -> task)
    case TaskRemoved(taskId) => Json.obj("response_type" -> "task_removed", "task" -> taskId)
    case TaskCompleted(taskId) => Json.obj("response_type" -> "task_completed", "task" -> taskId)
    case TaskUpdated(task) => Json.obj("response_type" -> "task_updated", "task" -> task)
    case State(list) => Json.obj("state" -> list)
  }*/
/*
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
  }*/

}