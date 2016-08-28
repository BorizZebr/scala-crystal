package crawling

import javax.inject.Inject

import akka.actor.{Actor, PoisonPill, Props}
import com.zebrosoft.crystal.dal.repos.{ChartsDao, CompetitorsDao, GoodsDao, ReviewsDao}
import com.zebrosoft.crystal.model.{Chart, Competitor, Good, Review}
import org.joda.time.LocalDate
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by borisbondarenko on 14.06.16.
  */
object PersisterActor {

  def props = Props[PersisterActor]

  trait Factory {
    def apply(): Actor
  }

  case class UpdateCompetitor(url: String, name: String)
  case class UpdateAmount(cmpId: Long, newAmount: Int, date: LocalDate)
  case class UpdateReviews(reviews: Seq[Review])
  case class UpdateGoods(goods: Seq[Good])
}

class PersisterActor @Inject()(
    competitorsRepo: CompetitorsDao,
    chartsRepo: ChartsDao,
    reviewsRepo: ReviewsDao,
    goodsRepo: GoodsDao)
    extends Actor {

  import PersisterActor._

  override def receive: Receive = {

    case UpdateCompetitor(url, name) =>
      competitorsRepo.getByUrl(url).map {
        // in case of we already have this comp, check the name
        // update if we need to
        case Some(comInDb) => if (comInDb.name != name) {
          val comToUpdate = comInDb.updateNameAndUrl(name, url)
          competitorsRepo.update(comToUpdate)
          Logger.info(s"Competitor ${comInDb.name} was renamed to $name")
        }
        // in case of none -- create in DB
        case None =>
          val comToCreate = Competitor(None, name, url, None, None, 0, 0)
          competitorsRepo.insert(comToCreate)
          Logger.info(s"Competitor $name was created")
      }
      self ! PoisonPill

    case UpdateAmount(cmpId, am, date) =>
      chartsRepo.getByCompetitorAndDate(cmpId, date).map {
        case Some(x) =>
          val chart = x.updateAmount(am)
          chartsRepo.update(chart)

        case None =>
          val chart = Chart(None, Some(cmpId), am, LocalDate.now)
          chartsRepo.insert(chart)
      }
      self ! PoisonPill

    case UpdateReviews(reviews) =>
      for {
        r <- reviews
        c <- reviewsRepo.contains(r)
        if !c
      } reviewsRepo.insert(r)
      self ! PoisonPill

    case UpdateGoods(goods) =>
      for {
        g <- goods
        c <- goodsRepo.contains(g)
        if !c
      } goodsRepo.insert(g)
      self ! PoisonPill
  }
}
