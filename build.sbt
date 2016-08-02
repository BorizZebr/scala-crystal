name := "scalaplayground"

version := "1.0"

lazy val `scalaplayground` = (project in file("."))
  .enablePlugins(PlayScala)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)

scalaVersion := "2.11.8"

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
  "org.scalatest" %% "scalatest" % "2.2.6" % "it,test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "it,test",
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.36-j7p" % "it,test",
  "org.mockito" % "mockito-core" % "1.10.19" % "it,test"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

unmanagedSourceDirectories in IntegrationTest <<=
  (baseDirectory in IntegrationTest)(base =>  Seq(base / "it"))

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

parallelExecution in Test := false

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  

fork in run := false