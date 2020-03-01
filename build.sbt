name := "rssFeednGT"

version := "0.1"

scalaVersion := "2.13.1"

scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= Seq(
  "com.rometools" % "rome" % "1.8.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.json4s" %% "json4s-native" % "3.7.0-M2",
  "org.jsoup" % "jsoup" % "1.10.3",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "com.typesafe.akka" %% "akka-actor" % "2.6.3",
  "com.typesafe.akka" %% "akka-stream" % "2.6.3",
)