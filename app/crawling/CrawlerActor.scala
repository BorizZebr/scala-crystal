package crawling

import javax.inject.Inject

import akka.actor._
import akka.stream.Materializer
import models.Competitor
import play.api.Logger
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 04.06.16.
  */
object CrawlerActor {

  trait Factory {
    def apply(): Actor
  }

  def props = Props[CrawlerActor]

  case class CrawlCompetitor(competitor: Competitor)

  case class CrawlComplete()
}

class CrawlerActor @Inject()(
    analizersFactory: ContentAnalizerActor.Factory,
    implicit val mat: Materializer) extends Actor with InjectedActorSupport {

  import ContentAnalizerActor._
  import CrawlerActor._
  import context.dispatcher

  private val httpClient: AhcWSClient = AhcWSClient()


  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    httpClient.close()
    super.postStop()
  }

  override def receive: Receive = {
    case CrawlCompetitor(c) =>
      for {
        main <- httpClient.url(c.url).get()
        goods <- getAdditionalMainPages(main)
        reviews <- getReviewsPages(c, main)
      } yield {
        val name = s"analizer-${c.id.getOrElse(0)}-${System.nanoTime}"
        val analizerActor = injectedChild(analizersFactory(), name)
        analizerActor ! AnalizeContent(c, goods, reviews)

        Logger.info(s"Analize $name")
      }

    case AnalizeComplete =>
      sender ! PoisonPill
      context.parent ! CrawlComplete

      Logger.info(s"PoisonPill $sender")
  }

  private def getAdditionalMainPages(main: WSResponse): Future[Seq[WSResponse]] = Future(Seq(main))

  private def getReviewsPages(c: Competitor, main: WSResponse): Future[Seq[WSResponse]] =
    Future.sequence(Seq(httpClient.url(s"${c.url}/feedbacks?status=m&from=0").get()))
}