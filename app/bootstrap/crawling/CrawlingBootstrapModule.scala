package bootstrap.crawling

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
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
    @Named("crawl-master") crawlMasterActor: ActorRef,
    @Named("competitors-persister") competitorsPersisterActor: ActorRef) {

  system.scheduler.schedule(3 seconds, 1 minutes, competitorsPersisterActor, PersistCompetitors)

  system.scheduler.scheduleOnce(10 seconds, crawlMasterActor, CrawlAllCompetitors)

  //val delay = (24 hours).toMillis - System.currentTimeMillis()
  system.scheduler.schedule(10 seconds, 24 hours, crawlMasterActor, CrawlAllCompetitors)
}