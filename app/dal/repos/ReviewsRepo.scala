package dal.repos

import javax.inject.{Inject, Singleton}

import dal.components.{CompetitorsDependentComponent, CrudComponent, DatabaseComponent}
import models.Review
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 27.05.16.
  */
@Singleton
class ReviewsRepo @Inject() (val dbConfig: DatabaseConfig[JdbcProfile])
  extends DatabaseComponent
  with CrudComponent
  with CompetitorsDependentComponent {

  import driver.api._

  class ReviewsTable(tag: Tag) extends Table[Review](tag, "REVIEW")
    with IdColumn[Review]
    with CompetitorDependantColumns[Review] {

    def author = column[String]("AUTHOR")
    def text = column[String]("TEXT")
    override def * = (id.?, competitorId.?, author, text, date) <> (Review.tupled, Review.unapply)
  }

  override type Entity = Review
  override type EntityTable = ReviewsTable
  override val table = TableQuery[ReviewsTable]
}