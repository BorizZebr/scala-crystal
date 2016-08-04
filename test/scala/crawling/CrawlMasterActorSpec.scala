package scala.crawling

import java.util.concurrent.{BlockingDeque, LinkedBlockingDeque}

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActor, TestActorRef, TestKit, TestProbe}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import crawling.CrawlMasterActor.CrawlAllCompetitors
import crawling.CrawlerActor
import crawling.CrawlerActor.CrawlCompetitor
import dal.repos.CompetitorsDao
import models.Competitor
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 03.08.16.
  */
class CrawlMasterActorSpec(_system: ActorSystem) extends TestKit(_system)
    with FlatSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll
    with ImplicitSender {

  import crawling.CrawlMasterActor
  import scala.concurrent.ExecutionContext.Implicits.global

  def this() = this(ActorSystem("CrawlMasterActorSpec"))

  val factoryMock = mock[CrawlerActor.Factory]
  val competitorsRepoMock = mock[CompetitorsDao]

  override def afterAll: Unit = system.terminate()

  it should "receive CrawlAllCompetitors and do nothing on empty competitors" in {
    // Arrange
    val t = TestProbe()
    when(competitorsRepoMock.getAll) thenReturn Future(Nil)
    val actor = system.actorOf(Props(new CrawlMasterActor(factoryMock, competitorsRepoMock)))

    // Act
    actor ! CrawlAllCompetitors

    // Assert
    t.expectNoMsg()
  }

  it should "recieve CrawlAllCompetitors and send CrawlCompetitor to created children" in {
    // Arrange
    val t = TestProbe()
    val tActor = t.ref

    object FakeCrawlerFactory extends CrawlerActor.Factory {
      override def apply() = new FakeActor()
      class FakeActor extends Actor {
        override def receive: Receive = {
          case msg => tActor ! msg
        }
      }
    }

    val testSeq = Seq(
      Competitor(Option(1), "name-1", "url-1"),
      Competitor(Option(2), "name-2", "url-2"),
      Competitor(Option(3), "name-3", "url-3"))
    when(competitorsRepoMock.getAll) thenReturn Future(testSeq)
    val actor = system.actorOf(Props(new CrawlMasterActor(FakeCrawlerFactory, competitorsRepoMock)))

    // Act
    actor ! CrawlAllCompetitors

    // Assert
    t.expectMsgAllOf(
      CrawlCompetitor(testSeq.head),
      CrawlCompetitor(testSeq(1)),
      CrawlCompetitor(testSeq(2)))
  }
}
