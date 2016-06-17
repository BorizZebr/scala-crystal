package crawling

import javax.inject.Inject

import akka.actor.{Actor, Props}
import crawling.CompetitorsPersisterActor.PersistCompetitors
import dal.repos.CompetitorsRepo
import models.Competitor
import play.api.{Configuration, Logger}

import scala.collection.JavaConversions._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by borisbondarenko on 14.06.16.
  */
object CompetitorsPersisterActor {

  def props = Props[CompetitorsPersisterActor]

  case class PersistCompetitors()
}

class CompetitorsPersisterActor @Inject()(
    configuration: Configuration,
    competitorsRepo: CompetitorsRepo)
    extends Actor {

  override def receive: Receive = {

    case PersistCompetitors =>
      // read the conf
      configuration.underlying.getObjectList("competitors").foreach { co =>
        val name = co.unwrapped()("name").toString
        val url = co.unwrapped()("url").toString

        competitorsRepo.getByUrl(url).map {
          // in case of we already have this comp, check the name
          // update if we need to
          case Some(comInDb) => if (comInDb.name != name) {
            val comToUpdate = Competitor(comInDb.id, name, url, comInDb.lastCrawlStart, comInDb.lastCrawlFinish)
            competitorsRepo.update(comToUpdate)
            Logger.info(s"Competitor ${comInDb.name} was renamed to $name")
          }
          // in case of none -- create in DB
          case None =>
            val comToCreate = Competitor(None, name, url, None, None)
            competitorsRepo.insert(comToCreate)
            Logger.info(s"Competitor $name was created")
        }
      }
  }
}
