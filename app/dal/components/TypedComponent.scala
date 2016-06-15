package dal.components

/**
  * Created by borisbondarenko on 27.05.16.
  */
trait TypedComponent extends DateColumnMapper  { self: DatabaseComponent =>

  import driver.api._

  type Entity
  type EntityTable <: Table[Entity]
  val table: TableQuery[EntityTable]
}
