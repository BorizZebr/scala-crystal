package dao

import javax.inject.{Inject, Singleton}

import models.Competitor
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 25.05.16.
  */
@Singleton()
class CompetitorsDAO @Inject()
(
  protected val dbConfigProvider: DatabaseConfigProvider)
    extends HasDatabaseConfigProvider[JdbcProfile]
    with CrudDAO[Competitor]{

  import driver.api._

  class CompetitorsTable(tag: Tag) extends GenericTable[Competitor](tag, "COMPETITOR") {
    def name = column[String]("NAME")
    def url = column[String]("URL")
    override def * = (id.?, name, url) <> (Competitor.tupled, Competitor.unapply)
  }

  override type EntityTable = CompetitorsTable
  override val table = TableQuery[CompetitorsTable]
}