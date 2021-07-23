package app

import api.ElasticSearchApi
import com.sksamuel.elastic4s.ElasticDsl.{DeleteIndexHandler, deleteIndex}
import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.requests.indexes.admin.DeleteIndexResponse
import play.api.Configuration

import scala.concurrent.Future

case class ElasticSearchTestEnv(config: Configuration) extends ElasticSearchApi(config) {

  def deleteListIndex(): Future[Response[DeleteIndexResponse]] = client.execute {
    deleteIndex(listIndex)
  }
}
