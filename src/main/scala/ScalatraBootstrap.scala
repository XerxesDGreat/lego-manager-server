import org.scalatra._
import javax.servlet.ServletContext

import com.ijosh.lego.controllers.{LegoApp, LegoRoutes, SetManagerController}
import org.slf4j.LoggerFactory
import slick.jdbc.SQLiteProfile.api._

class ScalatraBootstrap extends LifeCycle {
    val logger = LoggerFactory.getLogger(getClass)

    override def init(context: ServletContext) {
        val db = Database.forConfig("db.main")
        context.mount(new LegoApp(db), "/")
    }
}
