import models._
import play.api.libs.json.{Json, Writes}

/**
  * Created by borisbondarenko on 04.06.16.
  */
package object controllers {

  implicit val jodaDateWrites = Writes.jodaLocalDateWrites("yyyy-MM-dd")
  implicit val competitorsWrite = Json.writes[Competitor]
  implicit val reviewWrite = Json.writes[Review]
  implicit val goodWrite = Json.writes[Good]
  implicit val chartPointWrite = Json.writes[ChartPoint]

  implicit val packageWrite = Json.writes[Pckg]
  implicit val packageReads = Json.reads[Pckg]

  implicit val priceInfoWrite = Json.writes[PriceResponse]
  implicit val priceInfoReads = Json.reads[PriceResponse]

  implicit val responseTemplateWrites = Json.writes[ResponseTemplate]
  implicit val responseTemplateReads = Json.reads[ResponseTemplate]
}
