package persistence

import akka.contrib.persistence.mongodb.CanSuffixCollectionNames

import scala.util.Try

class SuffixCollectionNames extends CanSuffixCollectionNames {

  override def getSuffixFromPersistenceId(persistenceId: String): String = {
    val collectionName = Try(persistenceId.split("-").head).getOrElse("")
    collectionName
  }

  override def validateMongoCharacters(input: String): String = {
    // According to mongoDB documentation,
    // forbidden characters in mongoDB collection names (Unix) are /\. "$
    // Forbidden characters in mongoDB collection names (Windows) are /\. "$*<>:|?
    // in this example, we replace each forbidden character with an underscore character
    val forbidden = List('/', '\\', '.', ' ', '\"', '$', '*', '<', '>', ':', '|', '?')

    input.map { c => if (forbidden.contains(c)) '_' else c }
  }
}