package api

import com.typesafe.config.ConfigValueFactory
import play.api.ApplicationLoader
import play.api.Configuration
import play.api.inject._
import play.api.inject.guice._

import scala.jdk.CollectionConverters.IterableHasAsJava

class CustomApplicationLoader extends GuiceApplicationLoader() {
  override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {
    val config = context.initialConfiguration.get[String]("env") match {
      case "CLUSTER" =>
        cleanSeedNodes.withFallback(context.initialConfiguration)
      case _ =>
        context.initialConfiguration

    }
    initialBuilder
      .in(context.environment)
      .loadConfig(config)
      .overrides(overrides(context): _*)
  }

  def cleanSeedNodes: Configuration =
    Configuration("akka.cluster.seed-nodes" -> ConfigValueFactory.fromIterable(Nil.asJava))

}
