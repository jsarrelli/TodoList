package app

import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{AsyncDriver, MongoConnection}

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

trait MongoTestEnv {

  import TestApplication._

  val driver = new AsyncDriver()

  val connection: MongoConnection = Await.result(
    MongoConnection
      .fromString("mongodb://127.0.0.1:27018")
      .flatMap(parsedUri => driver.connect(parsedUri)),
    10 seconds
  )

  def cleanCollections(): Unit = {
    val db = Await.result(connection.database("todolist_test"), Duration.Inf)
    val collectionNames = Await.result(db.collectionNames, 10 seconds)
    val deletes = Future.traverse(collectionNames) { name =>
      db.collection[BSONCollection](name).delete().one(BSONDocument())
    }
    Await.result(deletes, 30 seconds)
  }

}
