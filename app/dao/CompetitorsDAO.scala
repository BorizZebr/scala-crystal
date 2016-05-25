package dao

import javax.inject.{Inject, Singleton}

import models.Competitor
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext


/**
  * Created by borisbondarenko on 25.05.16.
  */
@Singleton()
class CompetitorsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  class CompetitorsTable(tag: Tag) extends Table[Competitor](tag, "COMPETITOR") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def url = column[String]("URL")
    override def * = (id.?, name, url) <> (Competitor.tupled, Competitor.unapply)
  }

  val competitors = TableQuery[CompetitorsTable]

  def getAll: Future[Seq[Competitor]] =
    db.run(competitors.sortBy(_.id).result)

  def count(): Future[Int] =
    db.run(competitors.map(_.id).length.result)

  def insert(competitor: Competitor): Future[Unit] =
    db.run(competitors += competitor).map(_ => ())

  def insert(competitors: Seq[Competitor]): Future[Unit] =
    db.run(this.competitors ++= competitors).map(_ => ())
}
