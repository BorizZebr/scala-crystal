package crawling

import javax.inject.Inject

import akka.actor.{Actor, Props}
import com.zebrosoft.crystal.dal.repos.CompetitorsDao
import play.api.Logger
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
    crawlersFactory: CrawlerActor.Factory,
    competitorsRepo: CompetitorsDao) extends Actor with InjectedActorSupport{

  import CrawlMasterActor._
  import crawling.CrawlerActor.CrawlCompetitor
  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {

    case CrawlAllCompetitors =>
      for(cmttrs <- competitorsRepo.getAll) cmttrs.foreach { cmp =>
        //val name = s"crawler-${cmp.id.getOrElse(0)}-${System.nanoTime}"
        //val crawlerActor = injectedChild(crawlersFactory(), name)
        //crawlerActor ! CrawlCompetitor(cmp)

        //Logger.info(s"Crawl $name")
      }
  }
}
