import actors.{CreateList, CreateTask, ListCommand}
import controllers.Formatters
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

class FormattersSpec extends AnyFlatSpec with Matchers with Formatters {

  "Json Format" should
    "format Task" in {
    val command:ListCommand = CreateTask(12,13, "blabla")
    val taskJson = Json.toJson(command)
    (taskJson\"type").as[String] shouldBe "CREATE_TASK"
  }

  "serialize" should
    "serialize CreateList" in {

    val createList =  CreateList(1234,"My List")
    val jsonCommand = Json.toJson(createList)
      val jsonString =
        """"""
      val json = Json.parse(jsonString)
      json.as[CreateList].listId shouldBe 1234
    }


}
