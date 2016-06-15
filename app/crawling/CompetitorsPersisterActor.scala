package crawling

import javax.inject.Inject

import akka.actor.{Actor, Props}
import crawling.CompetitorsPersisterActor.PersistCompetitors
import play.api.{Configuration, Logger}
import scala.collection.JavaConversions._

/**
  * Created by borisbondarenko on 14.06.16.
  */
object CompetitorsPersisterActor {

  def props = Props[CompetitorsPersisterActor]

  case class PersistCompetitors()
}

class CompetitorsPersisterActor @Inject()(
    configuration: Configuration)
    extends Actor {

  override def receive: Receive = {

    case PersistCompetitors =>
      // read the conf
      configuration.underlying.getObjectList("competitors").foreach { co =>
        println(s"${co.unwrapped()("name")}")
      }
  }
}
