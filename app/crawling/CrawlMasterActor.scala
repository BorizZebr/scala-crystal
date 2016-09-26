package crawling

import javax.inject.Inject

import akka.actor.{Actor, Props}
import com.zebrosoft.crystal.dal.repos.{ChartsDao, CompetitorsDao, GoodsDao, ReviewsDao}
import crawling.current.CompetitorCrawler
import play.api.libs.concurrent.InjectedActorSupport

/**
  * Created by borisbondarenko on 04.06.16.
  */
object CrawlMasterActor {

  def props = Props[CrawlMasterActor]

  case object CrawlAllCompetitors

  case object CrawlComplete
}

class CrawlMasterActor @Inject()(
    competitorsRepo: CompetitorsDao,
    chartsRepo: ChartsDao,
    reviewsRepo: ReviewsDao,
    goodsRepo: GoodsDao
) extends Actor with InjectedActorSupport{

  import CrawlMasterActor._

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {

    case CrawlAllCompetitors =>
      competitorsRepo.getAll.map { cmpttr =>
        cmpttr.foreach { cmp =>
          val crawler = new CompetitorCrawler(cmp, chartsRepo, reviewsRepo, goodsRepo)
          crawler.crawlCompetitor()
        }
      }
  }
}
