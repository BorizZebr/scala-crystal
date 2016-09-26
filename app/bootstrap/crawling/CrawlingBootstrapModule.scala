package bootstrap.crawling

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.AbstractModule
import crawling.CompetitorsBootstraperActor.BootstrapCompetitors
import crawling.CrawlMasterActor.CrawlAllCompetitors
import org.joda.time.DateTime

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
    @Named("cmpttr-btstrpr") competitorsBootstrapActor: ActorRef) {

  system.scheduler.schedule(3 seconds, 1 minutes, competitorsBootstrapActor, BootstrapCompetitors)
  system.scheduler.scheduleOnce(10 seconds, crawlMasterActor, CrawlAllCompetitors)

  val delay = DateTime.now.plusDays(1).withTimeAtStartOfDay.getMillis - System.currentTimeMillis()
  system.scheduler.schedule(delay milliseconds, 24 hours, crawlMasterActor, CrawlAllCompetitors)
}