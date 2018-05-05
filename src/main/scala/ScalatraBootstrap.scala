import akka.actor.ActorSystem
import com.ijosh.lego.SharedLegoApp
import com.typesafe.config.ConfigFactory
import javax.servlet.ServletContext
import org.scalatra._
import org.slf4j.LoggerFactory
import slick.jdbc.SQLiteProfile.api._
import com.ijosh.lego.repository.{LegoData, MigrationManager}

class ScalatraBootstrap extends LifeCycle {
    val logger = LoggerFactory.getLogger(getClass)

    val config = ConfigFactory.load()

    override def init(context: ServletContext) {
        // @todo abstract this so we can replace db via configuration
        val hdb = Database.forURL("jdbc:h2:~/test")

        val mm = new MigrationManager(hdb)
        //mm.migrate();
        context.mount(new SharedLegoApp(hdb), "/")
    }
}
