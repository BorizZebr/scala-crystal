package dao

import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by borisbondarenko on 25.05.16.
  */
trait CrudDAO[A] extends DateColumnMapper { self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  abstract class GenericTable[B](tag: Tag, name: String) extends Table[A](tag, name) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  }

  type EntityTable <: GenericTable[A]
  val table: TableQuery[EntityTable]

  def getAll: Future[Seq[A]] =
    db.run(table.sortBy(_.id).result)

  def getById(id: Long): Future[Option[A]] =
    db.run(table.filter(_.id === id).result.headOption)

  def count(): Future[Int] =
    db.run(table.map(_.id).length.result)

  def insert(entity: A): Future[Unit] =
    db.run(table += entity).map(_ => ())

  def insert(entities: Seq[A]): Future[Unit] =
    db.run(this.table ++= entities).map(_ => ())

}
