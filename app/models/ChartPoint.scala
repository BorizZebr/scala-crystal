package models

import org.joda.time.DateTime

/**
  * Created by borisbondarenko on 21.05.16.
  */
case class ChartPoint(x: DateTime, amount: Int, change: Int)
