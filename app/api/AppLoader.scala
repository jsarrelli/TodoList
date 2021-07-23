package api

import com.typesafe.config.ConfigValueFactory
import play.api.inject.guice._
import play.api.{Application, ApplicationLoader, Configuration, Mode}

import scala.jdk.CollectionConverters.IterableHasAsJava

class AppLoader extends GuiceApplicationLoader() {
  override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {
    val config = context.environment.mode match {
      case Mode.Prod => cleanSeedNodes.withFallback(context.initialConfiguration)
      case _ => context.initialConfiguration
    }
    initialBuilder
      .in(context.environment)
      .loadConfig(config)
      .overrides(overrides(context): _*)
  }


  private def cleanSeedNodes: Configuration =
    Configuration("akka.cluster.seed-nodes" -> ConfigValueFactory.fromIterable(Nil.asJava))


}
