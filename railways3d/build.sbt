organization := "org.mmarini"

version := "0.1.0"

scalaVersion := "2.11.5"

name := "railways3d"

resolvers += "jMonkeyEngine" at "http://updates.jmonkeyengine.org/maven/"

resolvers += "nifty-maven-repo.sourceforge.net" at "http://nifty-gui.sourceforge.net/nifty-maven-repo"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3-1"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "com.jme3" % "jme3-core" % "3.0.10"

libraryDependencies += "com.jme3" % "jme3-desktop" % "3.0.10"

libraryDependencies += "com.jme3" % "jme3-lwjgl" % "3.0.10"

libraryDependencies += "com.jme3" % "jme3-terrain" % "3.0.10"

libraryDependencies += "com.jme3" % "jme3-blender" % "3.0.10"

libraryDependencies += "com.jme3" % "jme3-niftygui" % "3.0.10"

libraryDependencies += "org.bushe" % "eventbus" % "1.4"

libraryDependencies += "io.reactivex" %% "rxscala" % "0.23.0"

libraryDependencies += "io.reactivex" % "rxswing" % "0.21.0"

libraryDependencies +=  "org.json4s" %% "json4s-jackson" % "3.2.11"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % Test

libraryDependencies += "org.scalamock" %% "scalamock-core" % "3.1.2" % Test

libraryDependencies += "org.mockito" % "mockito-core" % "1.10.19"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.6" % Test


lazy val root = project in file(".")
