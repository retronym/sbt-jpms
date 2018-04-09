import com.lightbend.sbt.jpms._

name := "sawdust"

lazy val root = project.in(file(".")).aggregate(sawdustAlpha, userView)

organization in ThisBuild := "de.sormuras"

version in ThisBuild := "1.0-SNAPSHOT"

autoScalaLibrary in ThisBuild := false

crossPaths in ThisBuild := false

val slf4jDep = "org.slf4j" % "slf4j-api" % System.getProperty("slf4j.version", "1.8.0-beta1")
val apiguardianDep = "org.apiguardian" % "apiguardian-api" % System.getProperty("apiguardian.version", "1.0.0") % Test
val jupiterVersion = System.getProperty("jupiter.version", "5.1.0")
val junitJupiterApiDep = "org.junit.jupiter" % "junit-jupiter-api" % jupiterVersion % Test jpmsName "org.junit.jupiter.api"
val junitJupiterEngineDep = "org.junit.jupiter" % "junit-jupiter-engine" % jupiterVersion % Test
val junitJupiterConsoleDep = "org.junit.platform" % "junit-platform-console" % System.getProperty("platform.version", "1.1.0") % Test

// Non-forked runners in SBT don't support creation of a new ModuleLayer (https://docs.oracle.com/javase/9/docs/api/java/lang/ModuleLayer.html)
fork in run in ThisBuild := true
fork in test in ThisBuild := true

val sawdustAlpha = project.in(file("sawdust.alpha")).enablePlugins(JpmsPlugin).settings(
  jpmsModuleName := "sawdust.alpha",
  libraryDependencies ++= Seq(slf4jDep, apiguardianDep, junitJupiterApiDep, junitJupiterEngineDep, junitJupiterConsoleDep)
)

// This project defines its own
val userView = project.in(file("user.view")).enablePlugins(JpmsPlugin).dependsOn(sawdustAlpha).settings(
  jpmsModuleName in Test := "user.view",
  libraryDependencies ++= Seq(slf4jDep, apiguardianDep, junitJupiterApiDep, junitJupiterEngineDep, junitJupiterConsoleDep)
)
