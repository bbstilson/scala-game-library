# SGL: Scala Game Library

Scala Game Library (SGL) is a library for developing cross-platform 2D video
games in Scala. It provides a high-level API for building 2D games, and can
export games to the Desktop, Android, and the web.  More platforms, including
iOS and consoles, are on the roadmap.

SGL is still in development, but is intended to provide an extensive toolkit to
facilitate game development in Scala, with a layer of abstraction on top of the
platform-specific functionalities, and an out-of-the-box implementation for
many common features needed in games, such as physics, collision detection,
tilemaps, and scenes management.

## Features

The main selling point of SGL is to provide a platform-independent Scala game
framework to build games, and then deploy them to any platform. You can get
started by writing a core generic game implementation, and then configure any
backend with just a few lines of Scala.  You can iterate on your game by
running the AWT backend from `sbt`, which, depending on your configuration, is
as straighforward as:

    sbt mygame-awt/run

Thus, you can quickly iterate on your game locally, without the need to waste a
lot of time deploying to your target platforms, such as mobile or console.

The current implementation provides the following backends:

  * *Desktop* with JVM and AWT. This is mostly convenient during development,
    but can also serve as a final release if you are able to distribute your
    game to people that have a JVM. It will be cross-platform across Windows,
    Mac, and Linux.
  * *Android*. The Android backend is implemented with the native Android SDK
    for Java, which means that SGL supports Android natively.
  * *Web (Experimental)* with Scalajs. This is still an experimental backend
    and some features are missing. The web backend is implemented with scalajs
    and uses the HTML5 canvas for graphics.
  * *Native (Experimental)*. The native backend implemented with scala-native
    is able to generate a native executable that can then be run on the target
    platform without a JVM.  The support for native is not complete yet, but
    the current implementation is a proof of concept. Further extensions to
    this backend should enable SGL to eventually target iOS and various
    consoles.

## Work in Progress

This is a work in progress, so please don't hesitate to get in touch if you are
interested in writing a game in Scala.  This is not production ready yet and
things will need to be tweak in order to make them work, but I'm putting this
project out there as I think it has a good potential, and I'm looking for
feedback from people interested in such a library.

I'm developing new features on a need basis. I'm working on some Android games,
and I started to use this library as it was much nicer to build and test the
game on my Linux desktop, and only deploy to the phone for the final tests. I'm
constantly adding new features to the library based on my needs for my games,
but if you miss some other features, please let me know and I will add them!
You're also very welcome to contribute :)

If you check out the latest master branch, and find out that some stuff is not working
as expected, please understand that the project is evolving rapidly and I'm
likely just breaking existing stuff to try to improve the overall design. The
library does truly help in building actual games, and I successfully developed
one published [Android
game](https://play.google.com/store/apps/details?id=com.regblanc.winsmash) with
it. The library helped tremendously, by being entirely Scala-based and by
allowing for transparent porting from the Desktop to the Android platform.

## How to Write a Game with SGL

SGL is split across several sub-projects so that games built by the framework
only pack the necessary dependencies. The organization is to provide a `core`
project, which defines all the APIs, and roughly one backend per platform. A
game should then depend on both the core abstraction and the platform on which
it will deploys. Cross-platform games can be further split into smaller units,
with a cross-platform core logic that will only depends on the core SGL
abstractions and various platform-specific implementations. The small
[snake](examples/snake) project demonstrates how you can organize a game to be
cross platform. A more advanced example can be found by looking at the sources
of [Scalavator](https://github.com/regb/scalavator).

SGL is currently splited into the following sub-projects:

  * coreJVM, coreJS, coreNative (the core abstractions compiled for JVM,
    scalajs, and scala-native).  The [core](core/) project contains the
    abstract API for the platform as well as all the features that are
    platform-agnostic.
  * desktopAWT, depends on coreJVM and provide window and graphics with AWT.
  * desktopNative, depends on coreNative and use scala-native and OpenGL to
    build a native executable.
  * html5, depends on coreJS and use scala.js to generate a javascript game.
  * coreAndroid and android, for the android platform.
  * jvmShared, some non-core utilities shared by all JVM-based platform.

These projects are defined in the [built.sbt](build.sbt) file and have their
sources in each corresponding subdirectory. Android definitions are in a
[sub-directory](android/build.sbt) because the Android plugins does not
work with the most recent sbt version.

## Understanding the Versioning

For a long time, the only way to use SGL was to declare direct source
dependencies. That proved relatively unsuccessful and most of the Scala
community seems to belive that binary dependencies is the way to go. Since SGL
is still an early prototype, I wasn't very keen on publishing a stable version
as a binary that people could just depend on through Maven.

Nevertheless, to encourage more people to try SGL, I will start officially
releasing on Sonatype. The first release will be `0.0.1`, with the intent to be
clear that this is a highly experimental version. Future versions will simply
increase the last digit, i.e. `0.0.2` and then `0.0.3`. As long as SGL stays in
the `0.0` version line, there will be no guarantee on backward compatibility,
and each new version could break absolutely everything the previous version
introduced.  This is all in the name of velocity and innovation, of course. If you
do give a chance to SGL `0.0.X`, just be aware that there will be bugs, and updates
will likely break your code. You must be willing to actively engage with the
developers of SGL.

The eventual goal is to reach the `0.1` version line, which at that point will
be a more stable release and future updates would hopefully better respect
backward compatibility.

At the current time, version `0.0.1` is not yet released on Sonatype. Most of
the build config and code is there, but I want to make a few adjustments before
publishing.  Until then, you can use `publishLocal` to publish the version
locally and depend on it from your own games.

## Getting Started

I developed a small, but complete, [open-source
game](https://github.com/regb/scalavator) that can be used as a starting point
for configuration. The game is intended to demonstrate some of the features of
the library. Other subsets of the features are demonstrated in the
[examples](examples/) projects.

## Some Design Principles

* Games and only games. This is not a general media toolkit. The only things
  that should be build with this library are games.

* True Scala library. We want to expose a Scala-like library a much as
  possible. That means very clean and elegant design with type-safety. We are
  not going to compromise for compatibility with Java.

* Entirely cross-platform, no cheating. The core library should abstract
  everything and only exposes features that are truly cross-platform. Any
  platform-specific extensions should be provided in a type-safe way.

* Generic but pragmatic. We try to remain as generic as possible, in the sense
  that only features that would be useful for at least two different games
  would be integrated. However, we want to provide a very effective tool, so
  almost anything that is remotely useful for building games should be made
  available. Whenever a problem has many alternative implementations, we should
  try to provide an abstract interface, with each alternative implementation
  available and let the user select the one he prefers.

* 2D only. The library does not target 3D games. I believe there are an
  infinite number of wonderful games that can be build entirely in 2D, and I
  would rather focus on getting a great library to build 2D games than an
  average library to do everything.

* No magic build tricks. Everything is explicitly implemented in Scala. No
  additional code generator to handle the different platform, setting up a
  deployment platform should be simple enough to be done manually. That said,
  eventually a good sbt plugin would come in handy, as long as it is a light,
  and optional, layer on top.

## Gallery

SGL has been used to produce a small, but non-zero, number of published games.

* [WinSmash](http://regblanc.com/games/winsmash/), available for
[Android](https://play.google.com/store/apps/details?id=com.regblanc.winsmash)
* [Scalavator](http://regblanc.com/games/scalavator/), available for
[Android](https://play.google.com/store/apps/details?id=com.regblanc.scalavator)
and for the [Web](http://regblanc.com/games/scalavator/play.html). Code source
available on [GitHub](https://github.com/regb/scalavator).
* [Fish
  Escape](https://play.google.com/store/apps/details?id=com.limetalesgames.fishescapelite),
  a sequel to the Android title [Rat
  Trap](https://play.google.com/store/apps/details?id=com.regblanc.rattrap).

## Code Organization

I heavily use the cake pattern as a means to abstract the different backends
and to correctly modularize the system. A good article to introduce using the
cake pattern for dependencies injection is [this
one](http://jonasboner.com/real-world-scala-dependency-injection-di/).  There
is also a [great talk](https://www.youtube.com/watch?v=yLbdw06tKPQ) that
describes how to use the cake pattern, which closely ressembles our usage here.

## Developing

In order to run the tests for scala-native, you will need to follow the installation instructions on the Scala Native [website](https://scala-native.readthedocs.io/en/v0.3.9-docs/user/setup.html#installing-clang-and-runtime-dependencies) if you have not done so already.
