package com.ijosh.lego.repository

import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import slick.jdbc.SQLiteProfile.api._

import scala.io.Source
import com.ijosh.lego.repository.LegoData._
import slick.sql.FixedSqlAction
import slick.dbio.NoStream
import slick.dbio.Effect.Write

import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success}

class MigrationManager(db: Database) {
    protected implicit def executor = global

    val logger = LoggerFactory.getLogger(getClass)

    private val config:Config = ConfigFactory.load("fixture.conf")

    def migrate(): Unit = {
        db.run(DBIO.seq(
            LegoData.dropTables,
            LegoData.createTables,
            new ColorsMigration(config, db).getMigration,
            new ThemesMigration(config, db).getMigration,
            new PartCategoriesMigration(config, db).getMigration,
            new PartsMigration(config, db).getMigration,
            new InventoriesMigration(config, db).getMigration,
            new InventoryPartsMigration(config, db).getMigration
        )) onComplete {
            case Success(s) => logger.info("successful creating tables")
            case Failure(e) => logger.error("failed creating tables: " + e.getMessage)
        }
    }
}

abstract class Migration(config: Config) {
    def getMigration: FixedSqlAction[Option[Int], NoStream, Write] = ???
}

class ColorsMigration(config: Config, db: Database) extends Migration(config: Config) {
    val logger = LoggerFactory.getLogger(getClass)
    override def getMigration: FixedSqlAction[Option[Int], NoStream, Write] = {
        val fixtureFile = config.getString("fixtures.colors")
        val fixtureCSV: Iterator[String] = Source.fromResource(fixtureFile).getLines
        val colors = fixtureCSV.filter(l => !l.startsWith("id"))
            .map(l => l.split(","))
            .map(la => {
                val Array(id, name, rgb, isTrans) = la
                Color(id.toInt, rgb, isTrans, name)
            })
            .collect { case c: Color => c}
            .toList
        LegoData.colors ++= colors
    }
}

class ThemesMigration(config: Config, db: Database) extends Migration(config: Config) {
    val logger = LoggerFactory.getLogger(getClass)

    def conversion(splitArray: Array[String]): Model = {
        splitArray match {
            case array if array.length == 2 => {
                val Array(id, name) = array
                Theme(id.toInt, name, None)
            }
            case array if array.length == 3 => {
                val Array(id, name, parentId) = array
                Theme(id.toInt, name, Some(parentId.toInt))
            }
        }
    }

    val fixtureFile = "fixtures.themes"
    val headerRowStartsWith = "id"
    val tableQuery = LegoData.themes

    override def getMigration: FixedSqlAction[Option[Int], NoStream, Write] = {
        val fixtureFile = config.getString(this.fixtureFile)
        val fixtureCSV: Iterator[String] = Source.fromResource(fixtureFile).getLines
        val items = fixtureCSV.filter(l => !l.startsWith(headerRowStartsWith))
            .map(l => l.split(","))
            .map(la => this.conversion(la))
            .collect { case c: Theme => c}
            .toList
        tableQuery ++= items
    }
}

class PartCategoriesMigration(config: Config, db: Database) extends Migration(config: Config) {
    val logger = LoggerFactory.getLogger(getClass)

    val fixtureFile = "fixtures.partCategories"
    val headerRowStartsWith = "id"
    val tableQuery = LegoData.partCategories
    val separator = '|'

    override def getMigration: FixedSqlAction[Option[Int], NoStream, Write] = {
        val fixtureFile = config.getString(this.fixtureFile)
        val fixtureCSV: Iterator[String] = Source.fromResource(fixtureFile).getLines
        val items = fixtureCSV.filter(l => !l.startsWith(headerRowStartsWith))
            .map(l => l.split(this.separator))
            .map(la => {
                val Array(id, name) = la
                PartCategory(id.toInt, name)
            })
            .collect { case c: PartCategory => c}
            .toList
        tableQuery ++= items
    }
}

class PartsMigration(config: Config, db: Database) extends Migration(config: Config) {
    val logger = LoggerFactory.getLogger(getClass)

    val fixtureFile = "fixtures.parts"
    val headerRowStartsWith = "thumbnail"
    val tableQuery = LegoData.parts
    val separator = '|'

    def conversion(splitArray: Array[String]): Model = {
        splitArray match {
            case array if array.length == 3 => {
                val Array(partNum, partCatId, name) = array
                Part(partNum, name, partCatId.toInt, None)
            }
            case array if array.length == 4 => {
                val Array(thumbnail, partNum, partCatId, name) = array
                Part(partNum, name, partCatId.toInt, Some(thumbnail))
            }
        }
    }

    override def getMigration: FixedSqlAction[Option[Int], NoStream, Write] = {
        val fixtureFile = config.getString(this.fixtureFile)
        val fixtureCSV: Iterator[String] = Source.fromResource(fixtureFile).getLines
        val items = fixtureCSV.filter(l => !l.startsWith(headerRowStartsWith))
            .map(l => l.split(this.separator))
            .map(la => conversion(la))
            .collect { case c: Part => c}
            .toList
        tableQuery ++= items
    }
}

class InventoriesMigration(config: Config, db: Database) extends Migration(config: Config) {
    val logger = LoggerFactory.getLogger(getClass)

    val fixtureFile = "fixtures.inventories"
    val headerRowStartsWith = "id"
    val tableQuery = LegoData.inventories
    val separator = ','

    def conversion(splitArray: Array[String]): Model = {
        val Array(id, version, setNum) = splitArray
        Inventory(id.toInt, version.toInt, setNum)
    }

    override def getMigration: FixedSqlAction[Option[Int], NoStream, Write] = {
        val fixtureFile = config.getString(this.fixtureFile)
        val fixtureCSV: Iterator[String] = Source.fromResource(fixtureFile).getLines
        val items = fixtureCSV.filter(l => !l.startsWith(headerRowStartsWith))
            .map(l => l.split(this.separator))
            .map(la => conversion(la))
            .collect { case c: Inventory => c}
            .toList
        tableQuery ++= items
    }
}

class InventoryPartsMigration(config: Config, db: Database) extends Migration(config: Config) {
    val logger = LoggerFactory.getLogger(getClass)

    val fixtureFile = "fixtures.inventoryParts"
    val headerRowStartsWith = "inventory"
    val tableQuery = LegoData.inventoryParts
    val separator = ','

    def conversion(splitArray: Array[String]): Model = {
        val Array(inventoryId, partNum, colorId, quantity, isSpare) = splitArray
        InventoryPart(inventoryId.toInt, partNum, colorId.toInt, quantity.toInt, isSpare == "t")
    }

    override def getMigration: FixedSqlAction[Option[Int], NoStream, Write] = {
        val fixtureFile = config.getString(this.fixtureFile)
        val fixtureCSV: Iterator[String] = Source.fromResource(fixtureFile).getLines
        val items = fixtureCSV.filter(l => !l.startsWith(headerRowStartsWith))
            .map(l => l.split(this.separator))
            .map(la => conversion(la))
            .collect { case c: InventoryPart => c}
            .toList
        tableQuery ++= items
    }
}