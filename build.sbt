name := "scalaplayground"

version := "1.0"

lazy val `scalaplayground` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( cache , ws, specs2 % Test,
  // Database
  "com.typesafe.play" %% "play-slick" % "2.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2",
  "mysql" % "mysql-connector-java" % "6.0.2",
  // test
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.h2database" % "h2" % "1.4.191"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  

fork in run := false