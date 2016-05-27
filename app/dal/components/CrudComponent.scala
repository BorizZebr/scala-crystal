package dal.components

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 25.05.16.
  */
trait CrudComponent extends TypedComponent { self: DatabaseComponent =>

  import driver.api._

  trait IdColumn[Entity] extends Table[Entity] {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  }

  override type EntityTable <: IdColumn[Entity]

  def getAll: Future[Seq[Entity]] =
    db.run(table.sortBy(_.id).result)

  def getById(id: Long): Future[Option[Entity]] =
    db.run(table.filter(_.id === id).result.headOption)

  def count(): Future[Int] =
    db.run(table.map(_.id).length.result)

  def insert(entity: Entity): Future[Unit] =
    db.run(table += entity).map(_ => ())

  def insert(entities: Seq[Entity]): Future[Unit] =
    db.run(this.table ++= entities).map(_ => ())

}