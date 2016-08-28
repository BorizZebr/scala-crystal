name := "scala-playground"

version := "0.1.0-SNAPSHOT"

val Demo = config("demo") extend Runtime

lazy val `scalaplayground` = (project in file("."))
  .dependsOn(crystalDal)
  .enablePlugins(PlayScala)
  .configs(Demo)
  .settings(Defaults.itSettings: _*)

scalaVersion := "2.11.8"

lazy val crystalDal = RootProject(uri("git://github.com/BorizZebr/scala-crystal-dal.git#master"))

libraryDependencies ++= Seq( cache , ws,
  // database
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.typesafe.play" %% "play-slick" % "2.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2",
  //"mysql" % "mysql-connector-java" % "6.0.2",
  "com.h2database" % "h2" % "1.4.191",
  // crawling
  "org.jsoup" % "jsoup" % "1.9.2",
  // test
  "com.typesafe.akka" %% "akka-testkit" % "2.4.8" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test",
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.36-j7p" % "test",
  "org.mockito" % "mockito-core" % "1.10.19" % "test"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"
javaOptions in Demo += "-Dconfig.file=conf/application.demo.conf"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  

fork in run := true