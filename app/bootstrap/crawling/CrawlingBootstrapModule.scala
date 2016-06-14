package bootstrap.crawling

import javax.inject.Inject

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import crawling.CompetitorsPersisterActor.PersistCompetitors
import crawling.{CompetitorsPersisterActor, CrawlMasterActor}
import crawling.CrawlMasterActor.CrawlAllCompetitors
import dal.repos.CompetitorsRepo
import play.api.Play
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by borisbondarenko on 04.06.16.
  */
class CrawlingBootstrapModule extends AbstractModule {

  override def configure(): Unit =
    bind(classOf[CrawlingBoostrap]).asEagerSingleton()
}

private[crawling] class CrawlingBoostrap @Inject()(
    system: ActorSystem,
    competitorsRepo: CompetitorsRepo) {

  val competitorsPersisterActor = system.actorOf(CompetitorsPersisterActor.props, "competitors-persister-actor")
  val orchestratorActor = system.actorOf(CrawlMasterActor.props, "crawl-master")

  system.scheduler.schedule(3 seconds, 1 minutes, competitorsPersisterActor, PersistCompetitors)

  system.scheduler.scheduleOnce(10 seconds, orchestratorActor, CrawlAllCompetitors)

  val delay = (24 hours).toMillis - System.currentTimeMillis()
  system.scheduler.schedule(delay milliseconds, 24 hours, orchestratorActor, CrawlAllCompetitors)
}