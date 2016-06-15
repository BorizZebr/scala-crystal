package crawling

import javax.inject.Inject

import akka.actor.{Actor, PoisonPill, Props}
import crawling.CrawlMasterActor.{CrawlAllCompetitors, CrawlComplete}
import dal.repos.{ChartsRepo, CompetitorsRepo, GoodsRepo, ReviewsRepo}
import models.{Chart, Good, Review}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.InjectedActorSupport

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by borisbondarenko on 04.06.16.
  */
object CrawlMasterActor {

  def props = Props[CrawlMasterActor]

  case class CrawlAllCompetitors()

  case class CrawlComplete()//(idCompetitor: Int, subAmout: Int, reviews: Seq[Review], goods: Seq[Good], date: DateTime)
}

class CrawlMasterActor @Inject()(
    crawlersFactory: CrawlerActor.Factory,
    competitorsRepo: CompetitorsRepo,
    reviewsRepo: ReviewsRepo,
    goodsRepo: GoodsRepo,
    chartsRepo: ChartsRepo) extends Actor with InjectedActorSupport{

  import CrawlerActor._

  override def receive: Receive = {

    case CrawlAllCompetitors =>
      for(cmttrs <- competitorsRepo.getAll) cmttrs.foreach { cmp =>
        val name = s"crawler-${cmp.id.getOrElse(0)}-${System.nanoTime}"
        val crawlerActor = injectedChild(crawlersFactory(), name)
        crawlerActor ! CrawlCompetitor(cmp)

        Logger.info(s"Crawl $name")
      }

    case CrawlComplete() => //(idC, am, rw, gd, d) =>
//      competitorsRepo.getById(idC).map { c =>
//        chartsRepo.insert(Chart(None, Some(idC), am, d))
//        reviewsRepo.insert(rw)
//        goodsRepo.insert(gd)
//      }
      sender !
        PoisonPill
        Logger.info(s"PoisonPill $sender")

    case _ => Logger.warn("CrawlMasterActor doesn't receive this messages")
  }
}
