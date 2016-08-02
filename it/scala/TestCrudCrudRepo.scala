package scala

import dal.components.{CrudComponent, DalConfig}
import dal.repos.RepoBase

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 26.05.16.
  */
class TestCrudCrudRepo (dalConfig: DalConfig)
  extends RepoBase(dalConfig)
    with TestCrudDao {
}

trait TestCrudDao extends CrudComponent { self: DalConfig =>
  
  import driver.api._

  import scala.concurrent.ExecutionContext.Implicits.global

  case class TestEntity(id: Option[Long], name: String)

  class TestEntitiesTable(tag: Tag) extends Table[TestEntity](tag, tableName)
    with IdColumn[TestEntity] {

    def name = column[String]("NAME")
    override def * = (id.?, name) <> (TestEntity.tupled, TestEntity.unapply)
  }

  override type Entity = TestEntity
  override type EntityTable = TestEntitiesTable
  override val table = TableQuery[TestEntitiesTable]
  override val tableName = "TESTTABLE"

  override def contains(entity: TestEntity): Future[Boolean] = Future(false)
}
