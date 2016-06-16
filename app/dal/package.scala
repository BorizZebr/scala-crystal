/**
  * Created by borisbondarenko on 16.06.16.
  */
package object dal {

  implicit def executionContext = scala.concurrent.ExecutionContext.Implicits.global
}
