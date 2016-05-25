package dao

import org.joda.time.DateTime
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 25.05.16.
  */
trait CompetitorDependentDAO
  extends TypedDAO
  with DateColumnMapper { self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  trait CompetitorDependantColumns[B] extends Table[Entity] {
    def competitorId = column[Long]("COMPETITOR_ID")
    def date = column[DateTime]("DATE")
  }

  override type EntityTable <: CompetitorDependantColumns[Entity]

  def getByCompetitor(competitorId: Long, skip: Int, take: Int): Future[Seq[Entity]] =
    db.run(table
      .filter(_.competitorId === competitorId)
      .sortBy(_.date.desc)
      .drop(skip)
      .take(take)
      .result)
}