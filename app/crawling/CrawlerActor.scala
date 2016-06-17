package crawling

import javax.inject.Inject

import akka.actor._
import akka.stream.Materializer
import crawling.CrawlMasterActor.CrawlComplete
import models.Competitor
import play.api.Logger
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ahc.AhcWSClient

/**
  * Created by borisbondarenko on 04.06.16.
  */
object CrawlerActor {

  def props = Props[CrawlerActor]

  trait Factory {
    def apply(): Actor
  }

  case class CrawlCompetitor(competitor: Competitor)
  case class SuccessfulGet(response: WSResponse)
  case class FailedGet(e: Throwable)
}

class CrawlerActor @Inject()(implicit val mat: Materializer)extends Actor {

  import CrawlerActor._
  import akka.pattern.pipe
  import context.dispatcher

  private val httpClient: AhcWSClient = AhcWSClient()

  override def receive: Receive = {
    case CrawlCompetitor(c) =>
      httpClient.url(c.url).get().map { resp => SuccessfulGet(resp)
      }.recover {
        case e: Throwable => FailedGet(e)
      }.pipeTo(self)

    case SuccessfulGet(resp) =>
      context.parent ! CrawlComplete()

    case FailedGet(e) =>
      context.parent ! CrawlComplete()

    case invalid@_ => Logger.error(s"Crawler actor has received invalid message ${invalid}");
  }
}