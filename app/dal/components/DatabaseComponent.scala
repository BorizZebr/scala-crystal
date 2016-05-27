package dal.components

import com.google.inject.ImplementedBy
import config.DataAccessConfig
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 26.05.16.
  */
trait DatabaseComponent {

  val dalConfig: DalConfig

  protected val db = dalConfig.db

  protected val driver = dalConfig.driver
}

@ImplementedBy(classOf[DataAccessConfig])
trait DalConfig {

  val driver: JdbcProfile

  val db: JdbcProfile#Backend#Database
}
