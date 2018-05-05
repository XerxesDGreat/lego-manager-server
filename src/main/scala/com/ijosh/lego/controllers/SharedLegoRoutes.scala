package com.ijosh.lego.controllers

import com.ijosh.lego.repository.LegoData
import org.scalatra._
import org.slf4j.LoggerFactory
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

trait SharedLegoRoutes extends ScalatraBase with FutureSupport {

    val logger = LoggerFactory.getLogger(getClass)

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

    get("/sets/:id") {
        val id = params("id")
        db.run(LegoData.setsById(id).result).map(_.headOption).map {
            case Some(set) => Ok(set)
            case None => NotFound("set was not found")
        }
    }

    get("/sets") {
        val perPage = params.getOrElse("perPage", "25").toInt
        val currentPage = params.getOrElse("page", "1").toInt - 1
        db.run(LegoData.sets.drop(currentPage * perPage).take(perPage).result)
    }

    get("/themes/:themeId/sets") {
        val themeId = params.getOrElse("themeId", halt(400)).toInt
        db.run(LegoData.setsByThemeId(themeId).result)
    }

    get("/themes/:themeId") {
        val id = params("themeId").toInt
        val prom = Promise[ActionResult]()
        db.run(LegoData.themeById(id).result head) onComplete {
            case Success(theme) => {
                logger.info("success: theme " + theme.name)
                prom.complete(Try(Ok(theme)))
            }
            case Failure(e:NoSuchElementException) => prom.complete(Try(NotFound("unable to find theme by key %s" format id)))
            case Failure(e) => prom.complete(Try(InternalServerError(e getMessage)))
        }
        prom
    }

    get("/themes") {
        db.run(LegoData.themes.result)
    }

    get("/colors") {
        db.run(LegoData.colors.result)
    }

    get("/ping") {
        Ok("pong")
    }

    get("/parts") {
        db.run(LegoData.partCount.result) map (Ok(_))
    }

    get("/part_categories") {
        db.run(LegoData.partCategories.result)
    }

    get("/part_categories/:id/parts") {
        val id = params("id").toInt
        val prom = Promise[ActionResult]()
        db.run(LegoData.parts.filter(_.categoryId === id).length.result) onComplete {
            case Success(count) => prom.complete(Try(Ok(count)))
            case Failure(e) => prom.complete(Try(InternalServerError(e getMessage)))
        }
        prom
    }
}