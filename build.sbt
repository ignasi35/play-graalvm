// Force an `sbt reload` when `build.sbt` changes.
// Global / onChangedBuildSource := ReloadOnSourceChanges

val graalConfigurationDirectory = settingKey[File]("The directory where Graal configuration lives")
val graalResourcesConfigurationFile = settingKey[File]("The file for Graal's resource-config.json")
val graalReflectConfigurationFile = settingKey[File]("The file for Graal's reflect-config.json")


val GraalAkkaVersion = "0.5.0"
val GraalVersion = "19.2.1"

val svmGroupId = if (GraalVersion startsWith "19.2") "com.oracle.substratevm" else "org.graalvm.nativeimage"


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
        "-H:+AllowIncompleteClasspath",
        "-H:+TraceClassInitialization",
        /// The value for --initialize-at-build-time is still WIP and there are still a few issues to shave
        "--initialize-at-build-time=" + Seq(
          "scala",
          "org.slf4j",
          "akka.util",
          "play.core.utils.CaseInsensitiveOrdered$",
//          "play.api",
//          "play.core.server.AkkaHttpServer",
//          "play.core.server.AkkaHttpServer$",
        ).mkString(","),
        "--initialize-at-run-time=" + Seq(
          "play.utils.InlineCache",
        ).mkString(","),
         "--allow-incomplete-classpath",
         "--report-unsupported-elements-at-runtime",
        "-H:+ReportExceptionStackTraces",
        "-H:ResourceConfigurationFiles=" + graalResourcesConfigurationFile.value.getAbsolutePath,
        "-H:ReflectionConfigurationFiles=" + graalReflectConfigurationFile.value.getAbsolutePath,
    ),
    libraryDependencies ++= Seq(
        "org.graalvm.sdk" % "graal-sdk" % GraalVersion % "provided", // Only needed for compilation
        svmGroupId % "svm" % GraalVersion % "provided", // Only needed for compilation
        // Adds configuration to let GraalVM's Native-Image complete the build
        // GraalAkkaVersion="0.5.0" is only valid for Akka 2.5.x so I downgraded to Play 2.7.x
        "com.github.vmencik" %% "graal-akka-actor" % GraalAkkaVersion, // Only needed for compilation
        "com.github.vmencik" %% "graal-akka-stream" % GraalAkkaVersion, // Only needed for compilation
        "com.github.vmencik" %% "graal-akka-http" % GraalAkkaVersion, // Only needed for compilation
        "com.github.vmencik" %% "graal-akka-slf4j" % GraalAkkaVersion, // Only needed for compilation
        )
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
            // Play 2.7.x uses scalatestplus-play 4.0.x
            "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
        ),
        excludeDependencies ++= Seq(
            ExclusionRule("ch.qos.logback", "logback-classic")
        ),
    )
    .enablePlugins(GraalVMNativeImagePlugin)
    .settings(graalSettings)
