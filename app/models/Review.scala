package models

import org.joda.time.DateTime

/**
  * Created by borisbondarenko on 22.05.16.
  */
case class Review(id: Long, author: String, text: String, date: DateTime)
