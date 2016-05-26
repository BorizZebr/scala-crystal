package dal

import models.{ChartPoint, Chart}

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 27.05.16.
  */
trait ChartsComponent extends DatabaseComponent
  with CrudComponent
  with CompetitorsDependentComponent {

  import driver.api._

  class ChartsTable(tag: Tag) extends Table[Chart](tag, "CHART")
    with IdColumn[Chart]
    with CompetitorDependantColumns[Chart] {

    def amount = column[Int]("AMOUNT")

    override def * = (id.?, competitorId.?, amount, date) <>(Chart.tupled, Chart.unapply)
  }

  override type EntityTable = ChartsTable
  override type Entity = Chart
  override val table: driver.api.TableQuery[EntityTable] = TableQuery[ChartsTable]

  def getPoints(competitorId: Long, skip: Int, take: Int): Future[Seq[ChartPoint]] =
    getByCompetitor(competitorId, skip, take).map {
      case a if a.isEmpty => Nil
      case a =>
        val seq = a.reverse
        seq.tail.foldLeft(Seq(ChartPoint(seq.head.date, seq.head.amount, 0))) { (a, b) =>
          a :+ ChartPoint(b.date, b.amount, b.amount - a.head.amount)
        }
    }

}
