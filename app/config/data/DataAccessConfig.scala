package config.data

import javax.inject.{Inject, Singleton}

import dal.components.DalConfig
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 28.05.16.
  */
@Singleton
class DataAccessConfig @Inject() (dbConfigProvider: DatabaseConfigProvider) extends DalConfig {

  private val cfg = dbConfigProvider.get[JdbcProfile]

  override val db = cfg.db
  override val driver = cfg.driver
}

