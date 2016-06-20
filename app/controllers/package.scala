import models.{Review, ChartPoint, Good, Competitor}
import play.api.libs.json.{Json, Writes}
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Created by borisbondarenko on 04.06.16.
  */
package object controllers {

  implicit val jodaDateWrites = Writes.jodaLocalDateWrites("yyyy-MM-dd")
  implicit val competitorsWrite = Json.writes[Competitor]
  implicit val reviewWrite = Json.writes[Review]
  implicit val goodWrite = Json.writes[Good]
  implicit val chartPointWrite = Json.writes[ChartPoint]

//  val jodaDateForReviewWrites = Writes.jodaDateWrites("yyyy-MM-dd HH:mm")
//  implicit val reviewWrite: Writes[Review] = (
//    (__ \ "id").writeNullable[Long] ~
//    (__ \ "competitorId").writeNullable[Long] ~
//    (__ \ "author").write[String] ~
//    (__ \ "text").write[String] ~
//    (__ \ "date").write(jodaDateForReviewWrites))(unlift(Review.unapply))
}
