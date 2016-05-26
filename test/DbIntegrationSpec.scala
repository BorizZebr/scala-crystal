import org.scalatestplus.play.PlaySpec
import play.api.db.Databases

/**
  * Created by borisbondarenko on 26.05.16.
  */
class DbIntegrationSpec extends PlaySpec {

  Databases.withInMemory(){ database =>


  }
}
