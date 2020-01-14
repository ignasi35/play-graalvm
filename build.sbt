val graalConfigurationDirectory = settingKey[File]("The directory where Graal configuration lives")
val graalResourcesConfiguration = settingKey[File]("The file for Graal's resource-config.json")

val graalSettings = Seq(
    graalConfigurationDirectory := (Compile / resourceDirectory).value / "graal",
    graalResourcesConfiguration := graalConfigurationDirectory.value / "resource-config.json",
    // Application jar was not automatically included in the classpath
    scriptClasspathOrdering += {
        val jarFile = (Compile / packageBin / artifactPath).value
        jarFile -> ("lib/" + jarFile.getName)
    },
    graalVMNativeImageOptions ++= Seq(
        "--verbose",
        "--no-fallback",
        "-H:+ReportExceptionStackTraces",
        "-H:Log=registerResource:verbose", // log which resources get included into the image
        "-H:ResourceConfigurationFiles=" + graalResourcesConfiguration.value.getAbsolutePath,
    ),
)

lazy val root = (project in file("."))
    .enablePlugins(PlayScala)
    .settings(
        name := """play-graalvm""",
        organization := "com.lightbend.play",
        version := "1.0-SNAPSHOT",
        scalaVersion := "2.13.1",
        libraryDependencies ++= Seq(
            "com.softwaremill.macwire" %% "macros" % "2.3.3",
            "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
        )
    )
    .enablePlugins(GraalVMNativeImagePlugin)
    .settings(graalSettings)
