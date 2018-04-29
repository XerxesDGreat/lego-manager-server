package com.ijosh.lego

import com.ijosh.lego.controllers.LegoRoutes
import org.scalatra.test.scalatest._

class LegoManagerServletTests extends ScalatraFunSuite {

  addServlet(classOf[LegoRoutes], "/*")

  test("GET / on LegoManagerServlet should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

}
