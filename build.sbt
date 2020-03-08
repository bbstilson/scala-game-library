import sbtcrossproject.{crossProject, CrossType}

val scalaVer = "2.12.4"

val scalaNativeVer = "2.11.8"

lazy val commonSettings = Seq(
  version      := "0.0.1",
  organization := "com.regblanc.sgl",
  scalaVersion := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

// Android cannot run on Java8 so we stick with 2.11 and Java7. We
// need to build core separately for the right version.
val commonAndroidSettings = Seq(
    scalaVersion  := "2.11.8",
    scalacOptions += "-target:jvm-1.7",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    exportJars    := true
)

lazy val core = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./core"))
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-core",
  )
  .jvmSettings(
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  )
  .jsSettings(
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  )
  .nativeSettings(scalaVersion := scalaNativeVer)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native

// We need to build the core classes for a different JVM version for Android.
lazy val coreAndroid = (project in file("./core"))
  .settings(commonSettings: _*)
  .settings(commonAndroidSettings: _*)
  .settings(
    name         := "sgl-core-android",
    target       := baseDirectory.value / ".android" / "target",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  )

lazy val jvmShared = (project in file("./jvm-shared"))
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-jvmshared",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  )
  .dependsOn(coreJVM % "test->test;compile->compile")

lazy val jvmSharedAndroid = (project in file("./jvm-shared"))
  .settings(commonSettings: _*)
  .settings(commonAndroidSettings: _*)
  .settings(
    name   := "sgl-jvmshared-android",
    target := baseDirectory.value / ".android" / "target"
  )
  .dependsOn(coreAndroid)

lazy val desktopAWT = (project in file("./desktop-awt"))
  .settings(commonSettings: _*)
  .settings(
    name                := "sgl-desktop-awt",

    // Add .ogg support by using jorbis (transitive dependency) as a service provider
    libraryDependencies += "com.googlecode.soundlibs" % "vorbisspi" % "1.0.3-2", 
    // Additional audio format can be added in the game build config, with a standard spi for java sound API.
    libraryDependencies += "net.liftweb"   %% "lift-json" % "3.1.1",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  )
  .dependsOn(coreJVM % "test->test;compile->compile", jvmShared)

def ghProject(repo: String, version: String, name: String) = ProjectRef(uri(s"${repo}#${version}"), name)

lazy val desktopNative = (project in file("./desktop-native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings: _*)
  .settings(scalaVersion := scalaNativeVer)
  .settings(
    name := "sgl-desktop-native",
    libraryDependencies += "com.regblanc" %%% "native-sdl2" % "0.1",
    libraryDependencies += "com.regblanc" %%% "native-sdl2-image" % "0.1",
    libraryDependencies += "com.regblanc" %%% "native-opengl" % "0.1"
  )
  .dependsOn(coreNative)

lazy val html5 = (project in file("./html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "sgl-html5",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  )
  .dependsOn(coreJS % "test->test;compile->compile")

/*
 * I want to make sure that the test games are always buildable and
 * their build is kept in sync with the library. The original approach used to 
 * define the build for each game directly in its own subfolder.
 * 
 * Since I also wanted to directly depend on the sources (by opposition to a
 * published artifact), that required also declaring a sub project/ directory
 * that included the right version of sbt and every plugins. Then we could
 * define the dependency to the root library (i.e. 
 * ProjectRef(file("../../core"))). Dependency on source is important because
 * we want to be able to test modifications to the core library without having
 * to publish (even locally). With the source dependency, we can edit the
 * library code, and compile/run from the example game sbt console and verify
 * that the new version of the library still works.
 *
 * The drawback is of course the need to update the sbt version (the passage
 * from 0.13 to 1.0 was annoying) and the sbt plugins in the library as well
 * as in all example game projects. It would also be nice to be able, in one
 * command, to compile all games on all platform, to verify that changes are
 * not breaking anything too obvious. We can achieve this by defining the
 * projects at the top level, along with the core library definitions. We
 * use a set of settings (noPublishSettings) to avoid publishing the game
 * projects.
 */

lazy val noPublishSettings = Seq(
  publishArtifact := false,
  packagedArtifacts := Map.empty,
  publish := {},
  publishLocal := {}
)

lazy val helloCommonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val helloCore = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./examples/hello/core"))
  .settings(helloCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(name := "hello-core")
  .jvmSettings(
    exportJars := true
  )
  .nativeSettings(scalaVersion := scalaNativeVer)
  .jvmConfigure(_.dependsOn(coreJVM))
  .jsConfigure(_.dependsOn(coreJS))
  .nativeConfigure(_.dependsOn(coreNative))

lazy val helloCoreJVM = helloCore.jvm
lazy val helloCoreJS = helloCore.js
lazy val helloCoreNative = helloCore.native

lazy val helloAssets = file("./examples/hello/assets")

lazy val helloDesktopAWT = (project in file("./examples/hello/desktop-awt"))
  .settings(helloCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "hello-desktop-awt",
    fork in run := true,
    unmanagedResourceDirectories in Compile := Seq(helloAssets)
  )
  .dependsOn(coreJVM, desktopAWT, helloCoreJVM)

lazy val helloHtml5 = (project in file("./examples/hello/html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(helloCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "hello-html5",
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(coreJS, html5, helloCoreJS)

lazy val helloDesktopNative = (project in file("./examples/hello/desktop-native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(helloCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(scalaVersion := scalaNativeVer)
  .settings(
    name := "hello-desktop-native",
    unmanagedResourceDirectories in Compile := Seq(helloAssets),
    if(isLinux(OS))
      nativeLinkingOptions ++= Seq("-lGL")
    else if(isMac(OS))
      nativeLinkingOptions ++= Seq("-framework", "OpenGL")
    else
      ???
  )
  .dependsOn(coreNative, desktopNative, helloCoreNative)

lazy val snakeCommonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val snakeCore = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./examples/snake/core"))
  .settings(snakeCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(name := "snake-core")
  .nativeSettings(scalaVersion := scalaNativeVer)
  .jvmConfigure(_.dependsOn(coreJVM))
  .jsConfigure(_.dependsOn(coreJS))
  .nativeConfigure(_.dependsOn(coreNative))

lazy val snakeCoreJVM = snakeCore.jvm
lazy val snakeCoreJS = snakeCore.js
lazy val snakeCoreNative = snakeCore.native

lazy val snakeDesktopAWT = (project in file("./examples/snake/desktop-awt"))
  .settings(snakeCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name        := "snake-desktop-awt",
    fork in run := true
  )
  .dependsOn(coreJVM, desktopAWT, snakeCoreJVM)

lazy val snakeHtml5 = (project in file("./examples/snake/html5"))
  .enablePlugins(ScalaJSPlugin)
  .settings(snakeCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "snake-html5",
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(coreJS, html5, snakeCoreJS)

lazy val snakeDesktopNative = (project in file("./examples/snake/desktop-native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(snakeCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(scalaVersion := scalaNativeVer)
  .settings(
    name := "snake-desktop-native",
    if(isLinux(OS))
      nativeLinkingOptions += "-lGL"
    else if(isMac(OS))
      nativeLinkingOptions ++= Seq("-framework", "OpenGL")
    else
      ???
  )
  .dependsOn(coreNative, desktopNative, snakeCoreNative)

lazy val menuCommonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val menuCore = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./examples/menu/core"))
  .settings(menuCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(name := "menu-core")
  .jvmSettings(
    exportJars := true
  )
  .nativeSettings(scalaVersion := scalaNativeVer)
  .jvmConfigure(_.dependsOn(coreJVM))
  .jsConfigure(_.dependsOn(coreJS))
  .nativeConfigure(_.dependsOn(coreNative))

lazy val menuCoreJVM = menuCore.jvm
lazy val menuCoreJS = menuCore.js
lazy val menuCoreNative = menuCore.native

lazy val menuDesktopAWT = (project in file("./examples/menu/desktop-awt"))
  .settings(menuCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "menu-desktop-awt",
    fork in run := true
  )
  .dependsOn(coreJVM, desktopAWT, menuCoreJVM)

lazy val platformerCommonSettings = Seq(
  version        := "1.0",
  scalaVersion   := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val platformerCore = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("./examples/platformer/core"))
  .settings(platformerCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(name := "platformer-core")
  .nativeSettings(scalaVersion := scalaNativeVer)
  .jvmConfigure(_.dependsOn(coreJVM))
  .jsConfigure(_.dependsOn(coreJS))
  .nativeConfigure(_.dependsOn(coreNative))

lazy val platformerCoreJVM = platformerCore.jvm
lazy val platformerCoreJS = platformerCore.js
lazy val platformerCoreNative = platformerCore.native

lazy val platformerAssets = file("./examples/platformer/assets")

lazy val platformerDesktopAWT = (project in file("./examples/platformer/desktop-awt"))
  .settings(platformerCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name        := "platformer-desktop-awt",
    unmanagedResourceDirectories in Compile := Seq(platformerAssets),
    fork in run := true
  )
  .dependsOn(coreJVM, desktopAWT, platformerCoreJVM)

lazy val platformerDesktopNative = (project in file("./examples/platformer/desktop-native"))
  .enablePlugins(ScalaNativePlugin)
  .settings(platformerCommonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(scalaVersion := scalaNativeVer)
  .settings(
    name := "platformer-desktop-native",
    if(isLinux(OS))
      nativeLinkingOptions += "-lGL"
    else if(isMac(OS))
      nativeLinkingOptions ++= Seq("-framework", "OpenGL")
    else
      ???
  )
  .dependsOn(coreNative, desktopNative, platformerCoreNative)


lazy val OS = sys.props("os.name").toLowerCase
lazy val LinuxName = "Linux"
lazy val MacName = "Mac OS X"

def isLinux(name: String): Boolean = name.startsWith(LinuxName.toLowerCase)
def isMac(name: String): Boolean = name.startsWith(MacName.toLowerCase)

/** Currently, the native projects are not compiling due to binary incompatibility
 * with scalatest. Once this issue is resolved, we can revert back to the CI
 * command being `sbt test`. For now, this hackily ensures that we don't regress
 * being on everything else.
 * 
 * Missing projects from this command: coreNative, jvmSharedAndroid, platformerDesktopNative.
 */
lazy val verifyCiCommand = List(
  "coreJVM","desktopAWT","desktopNative","helloCoreJS","helloCoreJVM","helloCoreNative",
  "helloDesktopAWT","helloDesktopNative","helloHtml5","html5","jvmShared","menuCoreJS",
  "menuCoreJVM","menuCoreNative","menuDesktopAWT","platformerCoreJS","platformerCoreJVM",
  "platformerCoreNative","platformerDesktopAWT","snakeCoreJS","snakeCoreJVM","snakeCoreNative",
  "snakeDesktopAWT","snakeDesktopNative","snakeHtml5"
).map(_ + "/test").mkString("; ")

addCommandAlias("verifyCI", s"; $verifyCiCommand")
