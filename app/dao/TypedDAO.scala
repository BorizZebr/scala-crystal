package dao

import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 25.05.16.
  */
trait TypedDAO { self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  type Entity
  type EntityTable <: Table[Entity]
  val table: TableQuery[EntityTable]
}
