package dao

import java.sql.Date

import org.joda.time.DateTime
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 25.05.16.
  */
trait DateColumnMapper extends HasDatabaseConfig[JdbcProfile] {

  import driver.api._

  implicit val jodaTimeColumnType = MappedColumnType.base[DateTime, Date](
    jodaTime => new Date(jodaTime.getMillis),
    sqlDate => new DateTime(sqlDate)
  )
}
