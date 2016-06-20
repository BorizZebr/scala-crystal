package crawling

import javax.inject.Inject

import akka.actor.{Actor, Props}
import dal.repos._
import models._
import org.joda.time.{DateTime, LocalDate}
import play.api.{Configuration, Logger}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by borisbondarenko on 14.06.16.
  */
object PersisterActor {

  def props = Props[PersisterActor]

  case object PersistConfCompetitors
  case class UpdateAmount(cmpId: Long, newAmount: Int, date: LocalDate)
  case class UpdateReviews(reviews: Seq[Review])
  case class UpdateGoods(goods: Seq[Good])
}

class PersisterActor @Inject()(
    configuration: Configuration,
    competitorsRepo: CompetitorsRepo,
    chartsRepo: ChartsRepo,
    reviewsRepo: ReviewsRepo,
    goodsRepo: GoodsRepo)
    extends Actor {

  import PersisterActor._

  override def receive: Receive = {

    case PersistConfCompetitors =>
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

    case UpdateAmount(cmpId, am, date) =>
      chartsRepo.getByCompetitorAndDate(cmpId, date).map {
        case None =>
          val chart = Chart(None, Some(cmpId), am, LocalDate.now)
          chartsRepo.insert(chart)

        case Some(x) =>
          val chart = Chart(x.id, x.competitorId, am, x.date)
          chartsRepo.update(chart)
      }

    case UpdateReviews(reviews) =>
      reviewsRepo.insert(reviews)

    case UpdateGoods(goods) =>
      goodsRepo.insert(goods)
  }
}
