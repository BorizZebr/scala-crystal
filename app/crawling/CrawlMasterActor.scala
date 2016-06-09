package crawling

import javax.inject.Inject

import akka.actor.{Actor, PoisonPill, Props}
import crawling.CrawlMasterActor.{CrawlAllCompetitors, CrawlComplete}
import dal.repos.CompetitorsRepo
import play.api.Logger

/**
  * Created by borisbondarenko on 04.06.16.
  */
object CrawlMasterActor {

  def props = Props[CrawlMasterActor]

  case class CrawlAllCompetitors()

  case class CrawlComplete()
}

class CrawlMasterActor @Inject()(competitorsRepo: CompetitorsRepo) extends Actor{

  import CrawlerActor._

  override def receive: Receive = {

    case CrawlAllCompetitors =>
      for(cmttrs <- competitorsRepo.getAll) cmttrs.foreach {
        val crawlerActor = context.actorOf(CrawlerActor.props)
        crawlerActor ! CrawlCompetitor(_)
      }

    case CrawlComplete => sender ! PoisonPill

    case _ => Logger.warn("CrawlMasterActor doesn't receive this messages")
  }
}
