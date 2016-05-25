package bootstrap

import javax.inject.Inject

import dao.CompetitorsDAO
import models.Competitor

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

/**
  * Created by borisbondarenko on 25.05.16.
  */
private[bootstrap] class InitDevDatabase @Inject() (competitorsDAO: CompetitorsDAO) {

  def insert(): Unit = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext

    val insertInitialDataFuture = for {
      count <- competitorsDAO.count() if count == 0
      _ <- competitorsDAO.insert(InitDevDatabase.competitors)
    } yield ()

    Try(Await.result(insertInitialDataFuture, Duration.Inf))
  }

  insert()
}

private[bootstrap] object InitDevDatabase {

  val competitors = Seq(
    Competitor(Some(1), "Страна Повторяндия", "http://www.livemaster.ru/3165"),
    Competitor(Some(2), "Кристаль Систаль", "http://www.livemaster.ru/etnoart"),
    Competitor(Some(3), "Обыкновенные Обыкновенности", "http://www.livemaster.ru/embroidery")
  )
}
