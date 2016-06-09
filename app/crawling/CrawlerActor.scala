package crawling

import akka.actor._
import models.Competitor
import play.api.Logger

/**
  * Created by borisbondarenko on 04.06.16.
  */
object CrawlerActor {

  def props = Props[CrawlerActor]

  case class CrawlCompetitor(competitor: Competitor)
}

class CrawlerActor extends Actor {

  import CrawlerActor._

  override def receive: Receive = {
    case CrawlCompetitor(c) =>

    case _ => Logger.error("Crawler actor has received invalid message");
  }
}