package dal.repos

import javax.inject.{Inject, Singleton}

import dal.components.{CompetitorsDependentComponent, CrudComponent, DalConfig, DatabaseComponent}
import models.Review

/**
  * Created by borisbondarenko on 27.05.16.
  */
@Singleton
class ReviewsRepo @Inject() (val dalConfig: DalConfig)
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