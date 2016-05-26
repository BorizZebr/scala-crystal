package dal

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 26.05.16.
  */
trait DatabaseComponent {

  val dbConfig: DatabaseConfig[JdbcProfile]

  lazy val db = dbConfig.db

  lazy val driver = dbConfig.driver
}
