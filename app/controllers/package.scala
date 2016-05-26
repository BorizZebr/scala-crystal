import models.{ChartPoint, Good, Review, Competitor}
import play.api.libs.json.Json

/**
  * Created by borisbondarenko on 26.05.16.
  */
package object controllers {

  implicit val competitorsWrite = Json.writes[Competitor]
  implicit val reviewWrite = Json.writes[Review]
  implicit val goodWrite = Json.writes[Good]
  implicit val chartPointWrite = Json.writes[ChartPoint]
}
