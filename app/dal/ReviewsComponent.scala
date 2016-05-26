package dal

import models.Review

/**
  * Created by borisbondarenko on 27.05.16.
  */
trait ReviewsComponent extends DatabaseComponent
  with CrudComponent
  with CompetitorsDependentComponent { self: DatabaseComponent =>

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