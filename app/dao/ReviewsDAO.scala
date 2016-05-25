package dao

import javax.inject.{Inject, Singleton}

import models.Review
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 25.05.16.
  */
@Singleton()
class ReviewsDAO @Inject()
(
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val competitorsDAO: CompetitorsDAO)
    extends HasDatabaseConfigProvider[JdbcProfile]
    with CrudDAO
    with CompetitorDependentDAO {

  import driver.api._

  class ReviewsTable(tag: Tag) extends Table[Review](tag, "REVIEW")
    with IdColumn[Review]
    with CompetitorDependantColumns[Review] {

    def competitor = foreignKey("DIR_FK", competitorId, competitorsDAO.table)(_.id)

    def author = column[String]("AUTHOR")
    def text = column[String]("TEXT")
    override def * = (id.?, competitorId.?, author, text, date) <> (Review.tupled, Review.unapply)
  }

  override type Entity = Review
  override type EntityTable = ReviewsTable
  override val table = TableQuery[ReviewsTable]
}
