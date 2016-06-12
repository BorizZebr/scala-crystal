package bootstrap.init

import javax.inject.Inject

import com.google.inject.AbstractModule
import dal.repos.{ChartsRepo, GoodsRepo, ReviewsRepo, CompetitorsRepo}
import models.{Chart, Good, Review, Competitor}
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

/**
  * Created by borisbondarenko on 25.05.16.
  */
class InitDevDatabaseModule extends AbstractModule {

  override protected def configure(): Unit =
    bind(classOf[InitDevDatabase]).asEagerSingleton()
}

private[init] class InitDevDatabase @Inject()
(
  competitorsRepo: CompetitorsRepo,
  reviewsRepo: ReviewsRepo,
  goodsRepo: GoodsRepo,
  chartsRepo: ChartsRepo) {

  def insert(): Unit = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext

    val insertInitialDataFuture = for {
      count <- competitorsRepo.count() if count == 0
      _ <- competitorsRepo.insert(InitDevDatabase.competitors)
      _ <- reviewsRepo.insert(InitDevDatabase.reviews)
      _ <- goodsRepo.insert(InitDevDatabase.goods)
      _ <- chartsRepo.insert(InitDevDatabase.charts)
    } yield ()

    Try(Await.result(insertInitialDataFuture, Duration.Inf))
  }

  insert()
}

private[init] object InitDevDatabase {

  import scala.util.Random

  private val rnd = new Random

  private val imgs = Vector(
    "http://cs2.livemaster.ru/storage/47/af/9de4c21f35d0a0666166033e7bk9--materialy-dlya-tvorchestva-kaplya-10h14-mm.jpg",
    "http://cs1.livemaster.ru/storage/49/84/4db772cbc4a4b9adb3ffe4f5e5mg--materialy-dlya-tvorchestva-kaplya-10h14-mm.jpg",
    "http://cs5.livemaster.ru/storage/fa/1d/af2ddf3f5aa376b41ded4559ecpj--materialy-dlya-tvorchestva-kaplya-10h14-mm.jpg",
    "http://cs2.livemaster.ru/storage/80/b7/644a56d8715a37310e25119c42o9--materialy-dlya-tvorchestva-cushion-square-12.jpg",
    "http://cs5.livemaster.ru/storage/2b/b9/c33b57a4be93b770f629dc16dffm--materialy-dlya-tvorchestva-kaplya-10h14-mm.jpg",
    "http://cs1.livemaster.ru/storage/d7/97/b10f92c43aec275291bd163bd9cq--materialy-dlya-tvorchestva-kaplya-20h30-mm.jpg",
    "http://cs1.livemaster.ru/storage/82/4b/00ad19574a6d66ebfa49d1b90339--materialy-dlya-tvorchestva-krug-nezhnost-27-mm.jpg",
    "http://cs1.livemaster.ru/storage/b7/a3/b9b2b18f7c33e1069744726665nd--materialy-dlya-tvorchestva-navett-7h15-mm.jpg")

  private val urls = Vector(
    "http://www.livemaster.ru/item/12013949-materialy-dlya-tvorchestva-navett-7h15-mm-akva",
    "http://www.livemaster.ru/item/12013507-materialy-dlya-tvorchestva-navett-7h15-mm",
    "http://www.livemaster.ru/item/14904923-materialy-dlya-tvorchestva-navett-7h15-giatsint",
    "http://www.livemaster.ru/item/13918187-materialy-dlya-tvorchestva-rivoli-16-mm-alyj",
    "http://www.livemaster.ru/item/14408549-materialy-dlya-tvorchestva-navett-17h32-mm",
    "http://www.livemaster.ru/item/12349773-materialy-dlya-tvorchestva-oktagon-13h18-mm",
    "http://www.livemaster.ru/item/14994639-materialy-dlya-tvorchestva-krug-sapfir-27-mm",
    "http://www.livemaster.ru/item/12218931-materialy-dlya-tvorchestva-heart-19h20-mm")


  val competitors = Seq(
    Competitor(Some(1), "Страна Повторяндия", "http://www.livemaster.ru/3165", None, None),
    Competitor(Some(2), "Кристаль Систаль", "http://www.livemaster.ru/etnoart", None, None),
    Competitor(Some(3), "Обыкновенные Обыкновенности", "http://www.livemaster.ru/embroidery", None, None)
  )

  val reviews = for {
    c <- competitors
    r <- (1 to 20).map { x =>
      Review(
        None,
        c.id,
        author = LoremIpsum.words(2).split(' ').map(_ capitalize).mkString(" "),
        text = LoremIpsum.paragraphs(1),
        DateTime.now.minusDays(rnd.nextInt(30)).minusMinutes(rnd.nextInt(30)))
    }
  } yield r


  val goods = for {
    c <- competitors
    g <- (1 to 50).map { x =>
      Good(
        None,
        c.id,
        LoremIpsum.words(1) capitalize,
        rnd.nextDouble() * 1000,
        imgs(rnd.nextInt(imgs.length)),
        urls(rnd.nextInt(urls.length)),
        DateTime.now)
    }
  } yield g

  val charts = for {
    c <- competitors
    p <- (0 to 30).map { x=>
      Chart(
        None,
        c.id,
        rnd.nextInt(500),
        DateTime.now.minusDays(x))
    }
  } yield p
}
