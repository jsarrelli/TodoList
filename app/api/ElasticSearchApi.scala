package api

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import com.sksamuel.elastic4s._
import models.ListDescription
import play.api.Configuration

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class ElasticSearchApi @Inject()(configuration: Configuration) {

  import com.sksamuel.elastic4s.ElasticDsl._

  //TODO look for these configs on application.conf
  protected val url = configuration.get[String]("elastic-search.uri")
  protected val client = ElasticClient(JavaClient(ElasticProperties(url)))
  protected val listIndex = configuration.get[String]("elastic-search.list-index")

  generateListIndex()

  implicit object ListIdReader extends HitReader[ListDescription] {
    override def read(hit: Hit): Try[ListDescription] = Try {
      val source = hit.sourceAsMap
      ListDescription(source("listId").toString, source("name").toString)
    }
  }


  def getLists(): Future[List[ListDescription]] = client.execute(
    search(listIndex)
  ).map(_.result.to[ListDescription].toList)

  def indexListId(listId: String, name: String): Future[Response[IndexResponse]] =
    client.execute(
      indexInto(listIndex).fields(
        "listId" -> listId,
        "name" -> name
      )
    )

  def deleteList(listId: String): Unit = client.execute {
    deleteByQuery(listIndex, termQuery("listId", listId))
  }

  private def generateListIndex(): Unit = client.execute {
    createIndex(listIndex)
  }.await


}
