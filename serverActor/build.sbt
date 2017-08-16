name := "serverActor"

version := "1.0"

scalaVersion := "2.11.3"



// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor_2.11
libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.5.0"


// https://mvnrepository.com/artifact/com.typesafe.akka/akka-remote_2.11
libraryDependencies += "com.typesafe.akka" % "akka-remote_2.11" % "2.5.0"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.0.0" % "provided"

libraryDependencies += "org.apache.spark" % "spark-mllib_2.11" % "2.0.0" % "provided"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-protobuf_2.11
//libraryDependencies += "com.typesafe.akka" % "akka-protobuf_2.11" % "2.5.0"

//libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.1"

