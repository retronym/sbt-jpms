val commonSettings = Seq(
  organization := "com.lightbend.sbt",

  crossSbtVersions := Vector("0.13.17", "1.1.2"),

  scalacOptions ++= List(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-encoding", "UTF-8"
  )
)

// ---------------------------------------------------------------------------------------------------------------------
// sbt-scripted settings
val scriptedSettings = Seq(
  scriptedLaunchOpts += s"-Dproject.version=${version.value}",
  scriptedBufferLog := false
)

// ---------------------------------------------------------------------------------------------------------------------
// main settings
commonSettings

scriptedSettings

name := "sbt-jpms"

crossSbtVersions := Vector("0.13.17", "1.1.2")
// ---------------------------------------------------------------------------------------------------------------------
// publishing settings

sbtPlugin := true
//publishTo := Some(Classpaths.sbtPluginReleases) // THIS IS BAD IN THE CURRENT PLUGIN VERSION
publishMavenStyle := false

// bintray config
// bintrayOrganization := Some("retronym") //
bintrayRepository := "sbt-plugins"
bintrayPackage := "sbt-jpms"