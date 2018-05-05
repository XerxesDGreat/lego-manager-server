package com.ijosh.lego

import com.ijosh.lego.controllers.SharedLegoRoutes
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{FutureSupport, ScalatraServlet}
import slick.jdbc.SQLiteProfile.api._
import scala.concurrent.ExecutionContext.global

class SharedLegoApp(val db: Database) extends ScalatraServlet
    with FutureSupport
    with SharedLegoRoutes
    with JacksonJsonSupport {

    protected implicit lazy val jsonFormats: Formats = DefaultFormats

    protected implicit def executor = global

    before() {
        contentType = formats("json")
    }
}
