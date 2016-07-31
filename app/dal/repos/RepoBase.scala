package dal.repos

import dal.components.DalConfig
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

/**
  * Created by borisbondarenko on 01.08.16.
  */
class RepoBase (dalConfig: DalConfig)
    extends DalConfig {

  override val driver: JdbcProfile = dalConfig.driver
  override val db: JdbcBackend#DatabaseDef = dalConfig.db
}
