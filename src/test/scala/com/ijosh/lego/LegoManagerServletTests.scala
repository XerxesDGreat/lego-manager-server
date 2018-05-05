package com.ijosh.lego

import org.scalatra.test.scalatest._

class LegoManagerServletTests extends ScalatraFunSuite {

    val database

    addServlet(new SharedLegoApp(database), "/*")

    test("GET / on SharedLegoApp should return status 200") {
        get("/") {
            status should equal (200)
        }
    }

    test("GET /sets on SharedLegoApp should return status 200") {
        get("/") {
            status should equal (200)
        }
    }

}
