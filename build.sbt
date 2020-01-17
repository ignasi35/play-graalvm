val graalConfigurationDirectory = settingKey[File]("The directory where Graal configuration lives")
val graalResourcesConfigurationFile = settingKey[File]("The file for Graal's resource-config.json")
val graalReflectConfigurationFile = settingKey[File]("The file for Graal's reflect-config.json")

val graalSettings = Seq(
    graalConfigurationDirectory := (Compile / resourceDirectory).value / "graal",
    graalResourcesConfigurationFile := graalConfigurationDirectory.value / "resource-config.json",
    graalReflectConfigurationFile := graalConfigurationDirectory.value / "reflect-config.json",
    // Application jar was not automatically included in the classpath
    scriptClasspathOrdering += {
        val jarFile = (Compile / packageBin / artifactPath).value
        jarFile -> ("lib/" + jarFile.getName)
    },
    graalVMNativeImageOptions ++= Seq(
        "--verbose",
        "--no-fallback",
        "--allow-incomplete-classpath",
        "--report-unsupported-elements-at-runtime",
        "-H:+ReportExceptionStackTraces",
        "-H:Log=registerResource:verbose", // log which resources get included into the image
        "-H:ResourceConfigurationFiles=" + graalResourcesConfigurationFile.value.getAbsolutePath,
        "-H:ReflectionConfigurationFiles=" + graalReflectConfigurationFile.value.getAbsolutePath,

        "--initialize-at-build-time=" + Seq(
          "scala",
        ).mkString(","),
        "--initialize-at-run-time=" + Seq(
          "play.core.server.AkkaHttpServer$$anon$2",
          "com.typesafe.sslconfig.ssl.tracing.TracingSSLContext",
          "com.typesafe.config.impl.ConfigImpl$EnvVariablesHolder",
          "com.typesafe.config.impl.ConfigImpl$SystemPropertiesHolder",
        ).mkString(","),
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
            "org.slf4j" % "slf4j-jdk14" % "1.7.30",
            "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
        ),
        excludeDependencies ++= Seq(
            ExclusionRule("ch.qos.logback", "logback-classic")
        ),
    )
    .enablePlugins(GraalVMNativeImagePlugin)
    .settings(graalSettings)
