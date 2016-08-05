package scala.crawling

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import crawling.CrawlMasterActor.CrawlAllCompetitors
import crawling.CrawlerActor
import crawling.CrawlerActor.CrawlCompetitor
import dal.repos.CompetitorsDao
import models.Competitor
import org.mockito.Mockito._
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
    val actor = system.actorOf(Props(classOf[CrawlMasterActor], factoryMock, competitorsRepoMock))

    // Act
    actor ! CrawlAllCompetitors

    // Assert
    t.expectNoMsg()
  }

  it should "recieve CrawlAllCompetitors and send CrawlCompetitor to created children" in {
    // Arrange
    val t = TestProbe()

    val testSeq = Seq(
      Competitor(Option(1), "name-1", "url-1"),
      Competitor(Option(2), "name-2", "url-2"),
      Competitor(Option(3), "name-3", "url-3"))
    when(competitorsRepoMock.getAll) thenReturn Future(testSeq)

    val actor = system.actorOf(
      Props(classOf[CrawlMasterActor],
      new FakeActorFactory(t.ref) with CrawlerActor.Factory,
      competitorsRepoMock))

    // Act
    actor ! CrawlAllCompetitors

    // Assert
    t.expectMsgAllOf(
      CrawlCompetitor(testSeq.head),
      CrawlCompetitor(testSeq(1)),
      CrawlCompetitor(testSeq(2)))
  }
}