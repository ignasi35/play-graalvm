package loaders

import play.api.ApplicationLoader
import play.api.Application
import play.api.BuiltInComponentsFromContext
import play.api.ApplicationLoader.Context
import play.api.mvc.EssentialFilter
import play.filters.HttpFiltersComponents
import play.api.LoggerConfigurator
import play.api.routing.Router

import com.softwaremill.macwire._
import controllers.AssetsComponents
import controllers.HomeController

import router.Routes

class AppLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new AppComponents(context).application
  }
}

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context)
  with AssetsComponents
  with HttpFiltersComponents {

  private val homeController = wire[HomeController]

  override val router: Router = {
    val prefix: String = "/"
    wire[Routes]
  }
}