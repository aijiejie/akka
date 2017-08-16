name := """play-scala-starter-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.11.0"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.194"
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-remote_2.11
libraryDependencies += "com.typesafe.akka" % "akka-remote_2.11" % "2.5.0"
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor_2.11
libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.5.3"
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-protobuf_2.11
libraryDependencies += "com.typesafe.akka" % "akka-protobuf_2.11" % "2.5.0"