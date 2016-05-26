package dal

import models.Competitor

/**
  * Created by borisbondarenko on 26.05.16.
  */
trait CompetitorsComponent extends CrudComponent { self: DatabaseComponent =>

  import driver.api._

  class CompetitorsTable(tag: Tag) extends Table[Competitor](tag, "COMPETITOR")
    with IdColumn[Competitor] {

    def name = column[String]("NAME")
    def url = column[String]("URL")
    override def * = (id.?, name, url) <>(Competitor.tupled, Competitor.unapply)
  }

  override type Entity = Competitor
  override type EntityTable = CompetitorsTable
  override val table = TableQuery[CompetitorsTable]
}
