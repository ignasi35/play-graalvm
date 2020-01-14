lazy val root = (project in file("."))
    .enablePlugins(PlayScala)
    .settings(
        name := """play-graalvm""",
        organization := "com.lightbend.play",
        version := "1.0-SNAPSHOT",
        scalaVersion := "2.13.1",
        libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
    )
    .enablePlugins(GraalVMNativeImagePlugin)
    .settings(
        graalVMNativeImageOptions ++= Seq(
            "--verbose",
            "-H:+ReportExceptionStackTraces",
        )
    )
