package crawling

import akka.actor.{Actor, Props}
import models.Competitor
import play.api.libs.ws.WSResponse

/**
  * Created by borisbondarenko on 18.06.16.
  */
object ContentAnalizerActor {

  trait Factory {
    def apply(): Actor
  }

  def props = Props[ContentAnalizerActor]

  case class AnalizeContent(competitor: Competitor, goods: Seq[WSResponse], reviews: Seq[WSResponse])

  case class AnalizeComplete()
}

class ContentAnalizerActor extends Actor {

  import ContentAnalizerActor._

  override def receive: Receive = {
    case AnalizeContent(cmp, goods, reviews) =>
      sender ! AnalizeComplete

  }
}
