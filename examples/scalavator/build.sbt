val scalaVer = "2.12.0"

lazy val commonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val sglCoreJVM = ProjectRef(file("../.."), "coreJVM")
lazy val sglCoreJS = ProjectRef(file("../.."), "coreJS")
lazy val sglHtml5 = ProjectRef(file("../.."), "html5")
lazy val sglDesktop = ProjectRef(file("../.."), "desktopAWT")

lazy val core = (crossProject.crossType(CrossType.Pure) in file("./core"))
  .settings(commonSettings: _*)
  .settings(name := "scalavator-core")
  .jvmSettings(
    exportJars := true
  )
  .jvmConfigure(_.dependsOn(sglCoreJVM))
  .jsConfigure(_.dependsOn(sglCoreJS))

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val desktop = (project in file("./desktop"))
  .settings(commonSettings: _*)
  .settings(
    name := "scalavator-desktop"
  )
  .dependsOn(sglCoreJVM, sglDesktop, coreJVM)

lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "scalavator-html5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  )
  .dependsOn(sglCoreJS, sglHtml5, coreJS)
