val ScalatraVersion = "2.6.3"

organization := "com.ijosh"

name := "LEGO Manager Server"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.4"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
    "org.scalatra" %% "scalatra" % ScalatraVersion,
    "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
    "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "container",
    "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
    "org.scalatra" %% "scalatra-json" % ScalatraVersion,
    "org.json4s" %% "json4s-jackson" % "3.5.2",
    "com.typesafe.slick" %% "slick" % "3.2.3",
    "org.xerial" % "sqlite-jdbc" % "3.21.0"
)

enablePlugins(SbtTwirl)
enablePlugins(ScalatraPlugin)
