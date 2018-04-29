package com.ijosh.lego.controllers

import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._

// put into models dir

class SetManagerController extends ScalatraServlet with JacksonJsonSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  get("/") {
    SetData.all
  }

  before() {
    contentType = formats("json")
  }
}

// database?
object SetData {
  var all = List(
    Set("1234", "AT-AT"),
    Set("2345", "foo"),
    Set("4567", "bar")
  )
}