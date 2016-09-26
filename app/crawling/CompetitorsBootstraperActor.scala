package crawling

import javax.inject.Inject

import akka.actor.{Actor, Props}
import com.zebrosoft.crystal.dal.repos.CompetitorsDao
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport

/**
  * Created by borisbondarenko on 05.08.16.
  */
object CompetitorsBootstraperActor {
  def props = Props[CompetitorsBootstraperActor]
  case object BootstrapCompetitors
}

class CompetitorsBootstraperActor @Inject()(
    configuration: Configuration,
    competitorsRepo: CompetitorsDao) extends Actor
    with InjectedActorSupport {

  import CompetitorsBootstraperActor._

  import scala.collection.JavaConversions._

  override def receive: Receive = {

    case BootstrapCompetitors =>
      // read the conf
      configuration.underlying.getObjectList("competitors").foreach { co =>
        val cmpUrl = co.unwrapped()("url").toString
        val cmpName = co.unwrapped()("name").toString
        competitorsRepo.updateName(cmpUrl, cmpName)
      }
  }

}
