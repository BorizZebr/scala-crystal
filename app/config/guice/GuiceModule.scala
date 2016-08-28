package config.guice

import com.google.inject.AbstractModule
import com.zebrosoft.crystal.dal.components.DalConfig
import com.zebrosoft.crystal.dal.repos._
import config.data.DataAccessConfig
import crawling._
import play.api.libs.concurrent.AkkaGuiceSupport
import services._

/**
  * Created by borisbondarenko on 15.06.16.
  */
class GuiceModule extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {

    bind(classOf[DalConfig]).to(classOf[DataAccessConfig]).asEagerSingleton()
    bind(classOf[CompetitorsDao]).to(classOf[CompetitorsRepo]).asEagerSingleton()
    bind(classOf[ChartsDao]).to(classOf[ChartsRepo]).asEagerSingleton()
    bind(classOf[ReviewsDao]).to(classOf[ReviewsRepo]).asEagerSingleton()
    bind(classOf[GoodsDao]).to(classOf[GoodsRepo]).asEagerSingleton()
    bind(classOf[RespTemplatesDao]).to(classOf[RespTemplatesRepo]).asEagerSingleton()

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
    // fake price calculator service
    bind(classOf[PriceCalculatorService]).toInstance(FakePriceCalculator)
  }
}

class ProdModule extends GuiceModule {
  override def configure() = {
    super.configure()
    // real price calculator service
    bind(classOf[PriceCalculatorService]).to(classOf[ProdPriceCalculator])
  }
}