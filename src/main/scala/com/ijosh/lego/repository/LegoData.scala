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
    // Definition of the `sets` table. This contains information about each set in the LEGO
    // system.
    // CREATE TABLE sets (set_num VARCHAR(15), name VARCHAR(200), year INT, theme_id INT, num_parts INT);
    class Sets(tag: Tag) extends Table[(String, String, Int, Int, Int)](tag, "sets") {
        def id = column[String]("set_num", O.PrimaryKey)
        def name = column[String]("name")
        def year = column[Int]("year")
        def themeId = column[Int]("theme_id")
        def numParts = column[Int]("num_parts")

        def theme = foreignKey("theme_id_fk", themeId, themes)(_.id)

        def * = (id, name, year, themeId, numParts)
    }

    // Definition of the `parts` table. This contains information about each part in the LEGO
    // system.
    // CREATE TABLE parts (part_num VARCHAR(25), name VARCHAR(200), part_cat_id INT, thumbnail varchar(120));
    class Parts(tag: Tag) extends Table[(String, String, Int, String)](tag, "parts") {
        def id = column[String]("part_num", O.PrimaryKey)
        def name = column[String]("name")
        def categoryId = column[Int]("part_cat_id")
        def thumbnail = column[String]("thumbnail")

        def category = foreignKey("category_id_fk", categoryId, categories)(_.id)

        def * = (id, name, categoryId, thumbnail)
    }

    // Definition of the `inventories` table. This exists as a part of the relationship between the
    // `parts` and the `sets` tables since a single set will have multiple parts, potentially different
    // versions of the inventory containing different parts, and since parts will be used in multiple
    // sets
    // CREATE TABLE inventories (id INT PRIMARY KEY, version INT, set_num VARCHAR(15));
    class Inventories(tag: Tag) extends Table[(Int, Int, String)](tag, "inventories") {
        def id = column[Int]("id", O.PrimaryKey)
        def version = column[Int]("version")
        def setNum = column[String]("set_num")

        def * = (id, version, setNum)
    }

    // Definition of the `inventory_parts` table. This is another portion of the relationship
    // between `parts` and `sets` tables; this one defines the collection of parts for a given
    // inventory, and a set can have multiple inventories
    // CREATE TABLE inventory_parts (inventory_id INT, part_num VARCHAR(25), color_id INT, quantity INT, is_spare VARCHAR(1));
    class InventoryParts(tag: Tag) extends Table[(Int, String, Int, Int, String)](tag, "inventory_parts") {
        def inventoryId = column[Int]("inventory_id")
        def partNum = column[String]("part_num")
        def colorId = column[Int]("color_id")
        def quantity = column[Int]("quantity")
        def isSpare = column[String]("is_spare") // this is "boolean", with values of `t` or `f`

        def * = (inventoryId, partNum, colorId, quantity, isSpare)

        // relationships to other tables
        def inventory = foreignKey("inventory_id_fk", inventoryId, inventories)(_.id)
        def part = foreignKey("part_num_fk", partNum, parts)(_.id)
        def color = foreignKey("color_id_fk", colorId, colors)(_.id)
    }

    // Definition of the `colors` table. Contains information about the colors each part can be and is used
    // mainly by the inventory tables since a part can have multiple colors.
    // CREATE TABLE colors (rgb VARCHAR(6), id INT PRIMARY KEY, is_trans VARCHAR(1), name VARCHAR(20));
    class Colors(tag: Tag) extends Table[(Int, String, String, String)](tag, "colors") {
        def id = column[Int]("id", O.PrimaryKey)
        def rgb = column[String]("rgb")
        def isTrans = column[String]("is_trans") // this is "boolean", with values of `t` or `f`
        def name = column[String]("name")

        def * = (id, rgb, isTrans, name)
    }

    // Definition of the `part_categories` table. These are the "types" of parts and is referenced
    // in a straightforward way from the `parts` table
    // CREATE TABLE part_categories (id INT, name VARCHAR(50));
    class PartCategories(tag: Tag) extends Table[(Int, String)](tag, "part_categories") {
        def id = column[Int]("id", O.PrimaryKey)
        def name = column[String]("name")

        def * = (id, name)
    }

    // Definition of the `themes` table. These are the "types" of sets and are hierarchical in nature;
    // thus themes can have parents. A set will only belong to one theme
    // CREATE TABLE themes (id INT, name VARCHAR(50), parent_id INT);
    class Themes(tag: Tag) extends Table[(Int, String, Int)](tag, "themes") {
        def id = column[Int]("id", O.PrimaryKey)
        def name = column[String]("name")
        def parentId = column[Int]("parent_id")

        // relationship to self
        def parent = foreignKey("parent_id_fk", parentId, themes)(_.id)

        def * = (id, name, parentId)
    }

    // Table query for each of the defined tables
    val sets = TableQuery[Sets]
    val parts = TableQuery[Parts]
    val colors = TableQuery[Colors]
    val themes = TableQuery[Themes]
    val inventories = TableQuery[Inventories]
    val categories = TableQuery[PartCategories]
}
