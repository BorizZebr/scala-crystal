package bootstrap.crawling

import javax.inject.Inject

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import crawling.CrawlMasterActor
import crawling.CrawlMasterActor.CrawlAllCompetitors
import dal.repos.CompetitorsRepo
import play.api.Play

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

  val orchestratorActor = system.actorOf(CrawlMasterActor.props, "crawl-master")

  // read the conf
  Play.current.configuration.getConfigList("competitors").foreach { c =>

  }

  //orchestratorActor ! CrawlAllCompetitors
}