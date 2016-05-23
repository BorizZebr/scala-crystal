package controllers

import javax.inject.Inject

import models._
import models.misc.LoremIpsum
import org.joda.time.DateTime
import play.api.mvc._
import play.api.libs.json._

import scala.util.Random

class Application @Inject() (dataService: ) extends Controller {

  implicit val jodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def competitor = Action {
    implicit val competitorsWrite = Json.writes[Competitor]

    val competitors = Seq(
      Competitor(1, "Страна Повторяндия"),
      Competitor(2, "Кристаль Систаль"),
      Competitor(3, "Обыкновенные Обыкновенности")
    )

    Ok(Json.toJson(competitors))
  }

  def chart(id: Long) = Action {
    implicit val chartPointWrite = Json.writes[ChartPoint]

    val rnd = new Random()
    val res = (30 to 0 by -1).foldLeft(List((DateTime.now.minusDays(31), rnd.nextInt(500), 0)))  {
      (a, b) => {
        val amount = rnd.nextInt(500)
        (DateTime.now.minusDays(b), amount, amount - a.head._2) :: a
      }
    } map { x => ChartPoint(x._1, x._2, x._3)}

    Ok(Json.toJson(res))
  }

  def review(id: Long, skip: Int, take: Int) = Action {
    implicit val reviewWrite = Json.writes[Review]

    val rnd = new Random()

    val reviews = (0 until take).map {
      Review(_,
        author = LoremIpsum.words(2).split(' ').map(_ capitalize).mkString(" "),
        text = LoremIpsum.paragraphs(1),
        DateTime.now.minusDays(rnd.nextInt(30)))
    }

    Ok(Json.toJson(reviews))
  }

  def goods(id: Long, skip: Int, take: Int) = Action {
    implicit val goodWrite = Json.writes[Good]

    val rnd = new Random()

    val imgs = Vector(
      "http://cs2.livemaster.ru/storage/47/af/9de4c21f35d0a0666166033e7bk9--materialy-dlya-tvorchestva-kaplya-10h14-mm.jpg",
      "http://cs1.livemaster.ru/storage/49/84/4db772cbc4a4b9adb3ffe4f5e5mg--materialy-dlya-tvorchestva-kaplya-10h14-mm.jpg",
      "http://cs5.livemaster.ru/storage/fa/1d/af2ddf3f5aa376b41ded4559ecpj--materialy-dlya-tvorchestva-kaplya-10h14-mm.jpg",
      "http://cs2.livemaster.ru/storage/80/b7/644a56d8715a37310e25119c42o9--materialy-dlya-tvorchestva-cushion-square-12.jpg",
      "http://cs5.livemaster.ru/storage/2b/b9/c33b57a4be93b770f629dc16dffm--materialy-dlya-tvorchestva-kaplya-10h14-mm.jpg",
      "http://cs1.livemaster.ru/storage/d7/97/b10f92c43aec275291bd163bd9cq--materialy-dlya-tvorchestva-kaplya-20h30-mm.jpg",
      "http://cs1.livemaster.ru/storage/82/4b/00ad19574a6d66ebfa49d1b90339--materialy-dlya-tvorchestva-krug-nezhnost-27-mm.jpg",
      "http://cs1.livemaster.ru/storage/b7/a3/b9b2b18f7c33e1069744726665nd--materialy-dlya-tvorchestva-navett-7h15-mm.jpg")

    val urls = Vector(
      "http://www.livemaster.ru/item/12013949-materialy-dlya-tvorchestva-navett-7h15-mm-akva",
      "http://www.livemaster.ru/item/12013507-materialy-dlya-tvorchestva-navett-7h15-mm",
      "http://www.livemaster.ru/item/14904923-materialy-dlya-tvorchestva-navett-7h15-giatsint",
      "http://www.livemaster.ru/item/13918187-materialy-dlya-tvorchestva-rivoli-16-mm-alyj",
      "http://www.livemaster.ru/item/14408549-materialy-dlya-tvorchestva-navett-17h32-mm",
      "http://www.livemaster.ru/item/12349773-materialy-dlya-tvorchestva-oktagon-13h18-mm",
      "http://www.livemaster.ru/item/14994639-materialy-dlya-tvorchestva-krug-sapfir-27-mm",
      "http://www.livemaster.ru/item/12218931-materialy-dlya-tvorchestva-heart-19h20-mm")

    val goods = (0 until take).map {
      Good(_,
        LoremIpsum.words(1) capitalize,
        rnd.nextDouble() * 1000,
        imgs(rnd.nextInt(imgs.length)),
        urls(rnd.nextInt(urls.length)))
    }


    Ok(Json.toJson(goods))
  }
}