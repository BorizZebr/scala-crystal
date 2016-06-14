package crawling

import akka.actor.{Actor, Props}
import crawling.CompetitorsPersisterActor.PersistCompetitors
import play.api.Play

/**
  * Created by borisbondarenko on 14.06.16.
  */
object CompetitorsPersisterActor {

  def props = Props[CompetitorsPersisterActor]

  case class PersistCompetitors()
}

class CompetitorsPersisterActor extends Actor {

  override def receive: Receive = {

    case PersistCompetitors =>
      // read the conf
      Play.current.configuration.getConfigList("competitors").foreach { c =>

      }
  }
}
