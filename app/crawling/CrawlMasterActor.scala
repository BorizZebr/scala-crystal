package crawling

import javax.inject.Inject

import akka.actor.{Actor, PoisonPill, Props}
import crawling.CrawlMasterActor.{CrawlAllCompetitors, CrawlComplete}
import dal.repos.{ChartsRepo, CompetitorsRepo, GoodsRepo, ReviewsRepo}
import models.{Chart, Good, Review}
import org.joda.time.DateTime
import play.api.Logger

/**
  * Created by borisbondarenko on 04.06.16.
  */
object CrawlMasterActor {

  def props = Props[CrawlMasterActor]

  case class CrawlAllCompetitors()

  case class CrawlComplete(idCompetitor: Int, subAmout: Int, reviews: Seq[Review], goods: Seq[Good], date: DateTime)
}

class CrawlMasterActor @Inject()(
    competitorsRepo: CompetitorsRepo,
    reviewsRepo: ReviewsRepo,
    goodsRepo: GoodsRepo,
    chartsRepo: ChartsRepo) extends Actor{

  import CrawlerActor._

  override def receive: Receive = {

    case CrawlAllCompetitors =>
      for(cmttrs <- competitorsRepo.getAll) cmttrs.foreach {
        val crawlerActor = context.actorOf(CrawlerActor.props)
        crawlerActor ! CrawlCompetitor(_)
      }

    case CrawlComplete(idC, am, rw, gd, d) =>
      competitorsRepo.getById(idC).map { c =>
        chartsRepo.insert(Chart(None, Some(idC), am, d))
        reviewsRepo.insert(rw)
        goodsRepo.insert(gd)
      }
      sender ! PoisonPill

    case _ => Logger.warn("CrawlMasterActor doesn't receive this messages")
  }
}
