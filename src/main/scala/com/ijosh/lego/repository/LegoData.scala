package com.ijosh.lego.repository

import slick.jdbc.SQLiteProfile.api._
import slick.lifted.Tag


/**
  * All the... LEGO... Data...
  *
  * Class which provides an interface for talking to the databases regarding the LEGO
  * sets, parts, and their relationships. Deals exclusively with global state which is
  * shared by all users
  *
  * @author Josh Wickham
  * @since
  */
object LegoData {
    abstract class Model
    case class LegoSet(id: String, name: String, year: Int, themeId: Int, numParts: Int) extends Model
    case class Part (id: String, name: String, categoryId: Int, thumbnail: Option[String]) extends Model
    case class Inventory (id: Int, version: Int, setNum: String) extends Model
    case class InventoryPart (inventoryId: Int, partNum: String, colorId: Int, quantity: Int, isSpare: Boolean) extends Model
    case class Color (id: Int, rgb: String, isTrans: String, name: String) extends Model
    case class PartCategory (id: Int, name: String) extends Model
    case class Theme (id: Int, name: String, parentId: Option[Int]) extends Model

    // Definition of the `themes` table. These are the "types" of sets and are hierarchical in nature;
    // thus themes can have parents. A set will only belong to one theme
    // CREATE TABLE themes (id INT, name VARCHAR(50), parent_id INT);
    class Themes(tag: Tag) extends Table[Theme](tag, "themes") {
        def id = column[Int]("id", O.PrimaryKey)
        def name = column[String]("name")
        def parentId = column[Option[Int]]("parent_id")

        // relationship to self
        //def parent = foreignKey("parent_id_fk", parentId, themes)(_.id)

        def * = (id, name, parentId).mapTo[Theme]
    }

    // Definition of the `colors` table. Contains information about the colors each part can be and is used
    // mainly by the inventory tables since a part can have multiple colors.
    // CREATE TABLE colors (rgb VARCHAR(6), id INT PRIMARY KEY, is_trans VARCHAR(1), name VARCHAR(20));
    class Colors(tag: Tag) extends Table[Color](tag, "colors") {
        def id = column[Int]("id", O.PrimaryKey)
        def rgb = column[String]("rgb")
        def isTrans = column[String]("is_trans") // this is "boolean", with values of `t` or `f`
        def name = column[String]("name")

        def * = (id, rgb, isTrans, name).mapTo[Color]
    }

    // Definition of the `part_categories` table. These are the "types" of parts and is referenced
    // in a straightforward way from the `parts` table
    // CREATE TABLE part_categories (id INT, name VARCHAR(50));
    class PartCategories(tag: Tag) extends Table[PartCategory](tag, "part_categories") {
        def id = column[Int]("id", O.PrimaryKey)
        def name = column[String]("name")

        def * = (id, name).mapTo[PartCategory]
    }

    // Definition of the `parts` table. This contains information about each part in the LEGO
    // system.
    // CREATE TABLE parts (part_num VARCHAR(25), name VARCHAR(200), part_cat_id INT, thumbnail varchar(120));
    class Parts(tag: Tag) extends Table[Part](tag, "parts") {
        def id = column[String]("part_num", O.PrimaryKey)
        def name = column[String]("name")
        def categoryId = column[Int]("part_cat_id")
        def thumbnail = column[Option[String]]("thumbnail")

        def category = foreignKey("category_id_fk", categoryId, partCategories)(_.id)

        def * = (id, name, categoryId, thumbnail).mapTo[Part]
    }

    // Definition of the `sets` table. This contains information about each set in the LEGO
    // system.
    // CREATE TABLE sets (set_num VARCHAR(15), name VARCHAR(200), year INT, theme_id INT, num_parts INT);
    class Sets(tag: Tag) extends Table[LegoSet](tag, "sets") {
        def id = column[String]("set_num", O.PrimaryKey)
        def name = column[String]("name")
        def year = column[Int]("year")
        def themeId = column[Int]("theme_id")
        def numParts = column[Int]("num_parts")

        def theme = foreignKey("theme_id_fk", themeId, themes)(_.id)

        def * = (id, name, year, themeId, numParts).mapTo[LegoSet]
    }

    // Definition of the `inventories` table. This exists as a part of the relationship between the
    // `parts` and the `sets` tables since a single set will have multiple parts, potentially different
    // versions of the inventory containing different parts, and since parts will be used in multiple
    // sets
    // CREATE TABLE inventories (id INT PRIMARY KEY, version INT, set_num VARCHAR(15));
    class Inventories(tag: Tag) extends Table[Inventory](tag, "inventories") {
        def id = column[Int]("id", O.PrimaryKey)
        def version = column[Int]("version")
        def setNum = column[String]("set_num")

        def * = (id, version, setNum).mapTo[Inventory]
    }

    // Definition of the `inventory_parts` table. This is another portion of the relationship
    // between `parts` and `sets` tables; this one defines the collection of parts for a given
    // inventory, and a set can have multiple inventories
    // CREATE TABLE inventory_parts (inventory_id INT, part_num VARCHAR(25), color_id INT, quantity INT,
    //  is_spare VARCHAR(1));
    class InventoryParts(tag: Tag) extends Table[InventoryPart](tag, "inventory_parts") {
        def inventoryId = column[Int]("inventory_id")
        def partNum = column[String]("part_num")
        def colorId = column[Int]("color_id")
        def quantity = column[Int]("quantity")
        def isSpare = column[Boolean]("is_spare") // this is "boolean", with values of `t` or `f`

        def * = (inventoryId, partNum, colorId, quantity, isSpare).mapTo[InventoryPart]

        // relationships to other tables
        def inventory = foreignKey("inventory_id_fk", inventoryId, inventories)(_.id)
        def part = foreignKey("part_num_fk", partNum, parts)(_.id)
        def color = foreignKey("color_id_fk", colorId, colors)(_.id)
    }

    // Table query for each of the defined tables
    val sets = TableQuery[Sets]
    val parts = TableQuery[Parts]
    val inventories = TableQuery[Inventories]
    val inventoryParts = TableQuery[InventoryParts]
    val colors = TableQuery[Colors]
    val partCategories = TableQuery[PartCategories]
    val themes = TableQuery[Themes]

    def setsById(id:String) =  {
        sets.filter(_.id === id)
    }

    def themeById(id: Int) = {
        themes.filter(_.id === id)
    }

    def setsByThemeId(id:Int) = {
        sets.filter(_.themeId === id)
    }

    val allTables = themes.schema ++
        colors.schema ++
        partCategories.schema ++
        sets.schema ++
        parts.schema ++
        inventories.schema ++
        inventoryParts.schema

    val createTables = allTables.create

    val dropTables = allTables.drop

    val partCount = parts.length
}
