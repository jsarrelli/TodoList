package app

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.mockito.Mockito
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContextExecutor

object TestApplication {

  private val config = Configuration.empty
    .withFallback(mongoTestingDatabase)
    .withFallback(elasticSearchTest)
    .withFallback(Configuration(ConfigFactory.load()))

  val customApplicationLoader = new GuiceApplicationBuilder().loadConfig(config).build()

  val actorSystem: ActorSystem = customApplicationLoader.actorSystem

  val elasticSearch = Mockito.spy(ElasticSearchTestEnv(config))

  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  private def mongoTestingDatabase: Configuration =
    Configuration(
      "akka.contrib.persistence.mongodb.mongo.mongouri" -> "mongodb://localhost:27018/todolist_test"
    )

  private def elasticSearchTest: Configuration =
    Configuration("elastic-search.list-index" -> "lists_test")

}
