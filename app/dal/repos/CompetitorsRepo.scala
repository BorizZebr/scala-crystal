package dal.repos

import javax.inject.{Inject, Singleton}

import dal.components.{CrudComponent, DalConfig, DatabaseComponent}
import models.Competitor
import org.joda.time.DateTime

/**
  * Created by borisbondarenko on 26.05.16.
  */
@Singleton()
class CompetitorsRepo @Inject() (val dalConfig: DalConfig)
  extends DatabaseComponent
  with CrudComponent {

  import driver.api._

  class CompetitorsTable(tag: Tag) extends Table[Competitor](tag, "COMPETITOR")
    with IdColumn[Competitor] {

    def name = column[String]("NAME")
    def url = column[String]("URL")
    def crawlStart = column[DateTime]("LAST_CRAWL_START")
    def crawlFinish = column[DateTime]("LAST_CRAWL_FINISH")
    override def * = (id.?, name, url, crawlStart, crawlFinish) <> (Competitor.tupled, Competitor.unapply)
  }

  override type Entity = Competitor
  override type EntityTable = CompetitorsTable
  override val table = TableQuery[CompetitorsTable]
}
