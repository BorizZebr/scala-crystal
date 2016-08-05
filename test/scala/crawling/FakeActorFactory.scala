package scala.crawling

import akka.actor.{Actor, ActorRef}

/**
  * Created by borisbondarenko on 05.08.16.
  */
class FakeActorFactory(proxyActor: ActorRef) {
  class FakeActor extends Actor {
    override def receive: Receive = {
      case msg => proxyActor ! msg
    }
  }

  def apply(): Actor = new FakeActor()
}