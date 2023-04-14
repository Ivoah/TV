ThisBuild / organization := "net.ivoah"
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.0"

lazy val root = (project in file("."))
  .settings(
    name := "movies",
    idePackagePrefix := Some("net.ivoah.movies"),
    libraryDependencies ++= Seq(
      "net.ivoah" %% "vial" % "0.3.3",
      "mysql" % "mysql-connector-java" % "8.0.28",
      // "org.commonmark" % "commonmark" % "0.19.0",
      // "org.commonmark" % "commonmark-ext-gfm-strikethrough" % "0.19.0",
      "com.lihaoyi" %% "scalatags" % "0.12.0",
      "org.rogach" %% "scallop" % "4.1.0"
    )
  )
