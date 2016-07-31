package dal.components

import com.google.inject.ImplementedBy
import config.DataAccessConfig
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 26.05.16.
  */
@ImplementedBy(classOf[DataAccessConfig])
trait DalConfig {
  val driver: JdbcProfile
  val db: JdbcProfile#Backend#Database
}
