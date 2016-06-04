package crawling

import akka.actor.{Props, Actor}
import play.api.Logger

/**
  * Created by borisbondarenko on 04.06.16.
  */
object CrawlMasterActor {

  def props = Props[CrawlMasterActor]
}

class CrawlMasterActor extends Actor{

  override def receive: Receive = {
    case _ => Logger.warn("CrawlMasterActor doesn't receive any messages")
  }
}
