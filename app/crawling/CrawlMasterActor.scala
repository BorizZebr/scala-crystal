package crawling

import javax.inject.Inject

import akka.actor.{Actor, Props}
import crawling.CrawlMasterActor.CrawlAllCompetitors
import dal.repos.{ChartsRepo, CompetitorsRepo, GoodsRepo, ReviewsRepo}
import play.api.Logger
import play.api.libs.concurrent.InjectedActorSupport

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by borisbondarenko on 04.06.16.
  */
object CrawlMasterActor {

  def props = Props[CrawlMasterActor]

  case object CrawlAllCompetitors
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

    case CrawlComplete =>
  }
}
