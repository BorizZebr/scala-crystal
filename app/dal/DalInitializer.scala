package dal

import javax.inject.{Inject, Singleton}

import dal.components.DatabaseComponent
import dal.repos.{CompetitorsRepo, GoodsRepo, ReviewsRepo}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 27.05.16.
  */
@Singleton()
class DalInitializer @Inject() ( val dbConfig: DatabaseConfig[JdbcProfile],
                                 val competitorsRepo: CompetitorsRepo,
                                 val reviewsRepo: ReviewsRepo,
                                 val goodsRepo: GoodsRepo)
  extends DatabaseComponent {

  import driver.api._

  def init = (
      competitorsRepo.table.schema ++
      reviewsRepo.table.schema ++
      goodsRepo.table.schema) create

}
