package bootstrap.guice

import com.google.inject.AbstractModule
import crawling._
import dal.repos._
import play.api.libs.concurrent.AkkaGuiceSupport
import services._

/**
  * Created by borisbondarenko on 15.06.16.
  */
class GuiceModule extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {

    bind(classOf[CompetitorsDao]).to(classOf[CompetitorsRepo])
    bind(classOf[ChartsDao]).to(classOf[ChartsRepo])
    bind(classOf[ReviewsDao]).to(classOf[ReviewsRepo])
    bind(classOf[GoodsDao]).to(classOf[GoodsRepo])

    bindActor[CrawlMasterActor]("crawl-master")
    bindActor[CompetitorsBootstraperActor]("cmpttr-btstrpr")
    bindActorFactory[CrawlerActor, CrawlerActor.Factory]
    bindActorFactory[ReviewsAnalizerActor, ReviewsAnalizerActor.Factory]
    bindActorFactory[GoodsAnalizerActor, GoodsAnalizerActor.Factory]
    bindActorFactory[PersisterActor, PersisterActor.Factory]
  }
}

class DevModule extends GuiceModule {
  override def configure() = {
    super.configure()
    bind(classOf[PriceCalculatorService]).toInstance(FakePriceCalculator)
  }
}

class ProdModule extends GuiceModule {
  override def configure() = {
    super.configure()
    bind(classOf[PriceCalculatorService]).to(classOf[ProdPriceCalculator])
  }
}