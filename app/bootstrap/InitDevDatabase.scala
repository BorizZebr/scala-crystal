package bootstrap

import javax.inject.Inject

import dao.{ReviewsDAO, CompetitorsDAO}
import models._
import models.misc.LoremIpsum
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

/**
  * Created by borisbondarenko on 25.05.16.
  */
private[bootstrap] class InitDevDatabase @Inject()
(
  competitorsDAO: CompetitorsDAO,
  reviewsDAO: ReviewsDAO) {

  def insert(): Unit = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext

    val insertInitialDataFuture = for {
      count <- competitorsDAO.count() if count == 0
      _ <- competitorsDAO.insert(InitDevDatabase.competitors)
      _ <- reviewsDAO.insert(InitDevDatabase.reviews)
    } yield ()

    Try(Await.result(insertInitialDataFuture, Duration.Inf))
  }

  insert()
}

private[bootstrap] object InitDevDatabase {

  import scala.util.Random

  private val rnd = new Random

  val competitors = Seq(
    Competitor(Some(1), "Страна Повторяндия", "http://www.livemaster.ru/3165"),
    Competitor(Some(2), "Кристаль Систаль", "http://www.livemaster.ru/etnoart"),
    Competitor(Some(3), "Обыкновенные Обыкновенности", "http://www.livemaster.ru/embroidery")
  )

  private val compIds = competitors.map(_.id)
  private def randomComId: Option[Long] = {
    compIds(rnd.nextInt(compIds.size))
  }

  val reviews = (1 to 1000).map{ x=>
    Review(
      Some(x),
      randomComId,
      author = LoremIpsum.words(2).split(' ').map(_ capitalize).mkString(" "),
      text = LoremIpsum.paragraphs(1),
      DateTime.now.minusDays(rnd.nextInt(30)))
  }.toSeq
}
