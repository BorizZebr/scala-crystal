import sbt.Keys._

lazy val `scala-crystal` = (project in file("."))
  .dependsOn(crystalDal)
  .enablePlugins(PlayScala, JavaServerAppPackaging, DebianPlugin, SystemdPlugin)
  .settings(Defaults.itSettings: _*)
  .settings(
    name := "scala-crystal",
    version := "1.0",
    scalaVersion := "2.11.8",
    libraryDependencies ++= dependencies,
    unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" ),
    //javaOptions in Test += "-Dconfig.resource=application.test.conf",
    //javaOptions in run += "-Dconfig.resource=application.demo.conf",
    maintainer in Linux := "Boriz Zebr <borizzebr@egmail.com>",
    packageSummary in Linux := "Crystal Sister",
    packageDescription := "Crystal Sister",
    resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    //fork in run := true,
    javaOptions in Universal ++= Seq(
      // JVM memory tuning
      "-J-Xmx784m",
      "-J-Xms512m",

      // Since play uses separate pidfile we have to provide it with a proper path
      // name of the pid file must be play.pid
      s"-Dpidfile.path=/var/run/${packageName.value}/play.pid",

      // alternative, you can remove the PID file
      // s"-Dpidfile.path=/dev/null",

      // Use separate configuration file for production environment
      s"-Dconfig.file=/usr/share/${packageName.value}/conf/application.prod.conf",

      // Use separate logger configuration file for production environment
      s"-Dlogger.file=/usr/share/${packageName.value}/conf/logback.xml"
    )

  )


lazy val crystalDal = RootProject(uri("git://github.com/BorizZebr/scala-crystal-dal.git#master"))

lazy val dependencies = Seq( cache , ws, evolutions,
  // database
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.typesafe.play" %% "play-slick" % "2.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2",
  "org.postgresql" % "postgresql" % "9.4.1211",
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