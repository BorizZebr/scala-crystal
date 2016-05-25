package bootstrap

import com.google.inject.AbstractModule

/**
  * Created by borisbondarenko on 25.05.16.
  */
class InitDevDatabaseModule extends AbstractModule {
  override protected def configure(): Unit = {
    bind(classOf[InitDevDatabase]).asEagerSingleton()
  }
}
