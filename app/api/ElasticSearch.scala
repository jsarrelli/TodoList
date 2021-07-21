package api

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, Hit, HitReader}
import models.ListDescription

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object ElasticSearch {

  import com.sksamuel.elastic4s.ElasticDsl._

  //TODO look for these configs on application.conf
  private val url = Option(System.getenv("ELASTIC_SEARCH_URL")).getOrElse("http://localhost:9200")
  private val client = ElasticClient(JavaClient(ElasticProperties(url)))
  private val listIndex = "lists"

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

  def indexListId(listId: String, name: String): Unit = client.execute(
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
