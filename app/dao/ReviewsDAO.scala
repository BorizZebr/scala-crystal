package dao

import javax.inject.{Inject, Singleton}

import models.Review
import org.joda.time.DateTime
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 25.05.16.
  */
@Singleton()
class ReviewsDAO @Inject()
(
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val competitorsDAO: CompetitorsDAO)
    extends HasDatabaseConfigProvider[JdbcProfile]
    with CrudDAO[Review]{

  import driver.api._

  class ReviewsTable(tag: Tag) extends GenericTable[Review](tag, "REVIEW") {
    def competitorId = column[Long]("COMPETITOR_ID")
    def author = column[String]("AUTHOR")
    def text = column[String]("TEXT")
    def date = column[DateTime]("DATE")
    def competitor = foreignKey("DIR_FK", competitorId, competitorsDAO.table)(_.id)
    override def * = (id.?, competitorId.?, author, text, date) <> (Review.tupled, Review.unapply)
  }

  override type EntityTable = ReviewsTable
  override val table = TableQuery[ReviewsTable]

  def getByCompetitor(competitorId: Long, skip: Int, take: Int): Future[Seq[Review]] =
    db.run(table
      .filter(_.competitorId === competitorId)
      .sortBy(_.date.desc)
      .drop(skip)
      .take(take)
      .result)
}
