package com.ijosh.lego.controllers

import org.scalatra.{FutureSupport, ScalatraBase, ScalatraServlet}
import slick.jdbc.SQLiteProfile.api._
import com.ijosh.lego.repository.LegoData
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.JacksonJsonSupport

import scala.concurrent.ExecutionContext.Implicits.global

trait LegoRoutes extends ScalatraBase with FutureSupport {

    def db: Database

    // basic index doesn't need to do anything fancy... and it doesn't
    // ... unless you count the ASCII art, that is
    //
    // which I don't, because it's just copy-paste.
    get("/") {
        """
           .----------------. .----------------. .----------------. .----------------.
          | .--------------. | .--------------. | .--------------. | .--------------. |
          | |   _____      | | |  _________   | | |    ______    | | |     ____     | |
          | |  |_   _|     | | | |_   ___  |  | | |  .' ___  |   | | |   .'    `.   | |
          | |    | |       | | |   | |_  \_|  | | | / .'   \_|   | | |  /  .--.  \  | |
          | |    | |   _   | | |   |  _|  _   | | | | |    ____  | | |  | |    | |  | |
          | |   _| |__/ |  | | |  _| |___/ |  | | | \ `.___]  _| | | |  \  `--'  /  | |
          | |  |________|  | | | |_________|  | | |  `._____.'   | | |   `.____.'   | |
          | |              | | |              | | |              | | |              | |
          | '--------------' | '--------------' | '--------------' | '--------------' |
           '----------------' '----------------' '----------------' '----------------'
        """
    }

    get("/sets") {
        db.run(LegoData.sets.result)
    }
}

class LegoApp(val db: Database) extends ScalatraServlet
    with FutureSupport
    with LegoRoutes
    with JacksonJsonSupport {

    protected implicit lazy val jsonFormats: Formats = DefaultFormats
    protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

    before() {
        contentType = formats("json")
    }
}