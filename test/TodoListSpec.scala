
import controllers.Formatters
import models.TodoList
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TodoListSpec extends AnyFlatSpec with Matchers with Formatters {
  val list = TodoList(12, "test")

  "new task" should
    "add task at the end" in {
    val updatedList = list
      .newTask(1, "task1")
      .newTask(2, "task2")
    updatedList.tasks.map(_.taskId) shouldBe List(1, 2)
    val updateList2 = updatedList.newTask(3, "task3")
    updateList2.tasks.map(_.taskId) shouldBe List(1, 2, 3)
  }

  "remove" should "remove task" in {
    val updatedList = list
      .newTask(1, "task1")
      .newTask(2, "task2")
      .newTask(3, "task3")
      .removeTask(3)
    updatedList.tasks.map(_.taskId) shouldBe List(1, 2)
  }

  it should "remove task at the middle while keeping order" in {
    val updatedList = list
      .newTask(1, "task1")
      .newTask(2, "task2")
      .newTask(3, "task3")
      .removeTask(2)
    updatedList.tasks.map(_.taskId) shouldBe List(1, 3)
    updatedList.tasks.map(_.order) shouldBe List(0, 1)
  }
  it should "remove first task while keeping order" in {
    val updatedList = list
      .newTask(1, "task1")
      .newTask(2, "task2")
      .newTask(3, "task3")
      .removeTask(1)
    updatedList.tasks.map(_.taskId) shouldBe List(2, 3)
    updatedList.tasks.map(_.order) shouldBe List(0, 1)
  }

  "update task order" should "put some inner category as first" in{
    val updatedList = list
      .newTask(1, "task1")
      .newTask(2, "task2")
      .newTask(3, "task3")
      .newTask(4, "task4")
      .updateTaskOrder(2,0)
    updatedList.tasks.map(task => (task.taskId,task.order)) shouldBe List((2,0),(1,1),(3,2),(4,3))
  }

  it should "take some inner category to the back" in {
    val updatedList = list
      .newTask(1, "task1")
      .newTask(2, "task2")
      .newTask(3, "task3")
      .newTask(4, "task4")
      .updateTaskOrder(2,3)
    updatedList.tasks.map(task => (task.taskId,task.order)) shouldBe List((1,0),(3,1),(4,2),(2,3))
  }

  it should "take last category and put it at the middle" in {
    val updatedList = list
      .newTask(1, "task1")
      .newTask(2, "task2")
      .newTask(3, "task3")
      .newTask(4, "task4")
      .updateTaskOrder(4,1)
    updatedList.tasks.map(task => (task.taskId,task.order)) shouldBe List((1,0),(4,1),(2,2),(3,3))
  }

}
