package app

import api.ElasticSearchApi
import com.sksamuel.elastic4s.ElasticDsl.{DeleteIndexHandler, deleteIndex}
import play.api.Configuration

case class ElasticSearchTestEnv(config: Configuration) extends ElasticSearchApi(config) {

  def deleteListIndex(): Unit = client.execute {
    deleteIndex(listIndex)
  }
}
