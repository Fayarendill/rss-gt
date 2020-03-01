name := "rssFeednGT"

version := "0.1"

scalaVersion := "2.13.1"

scalacOptions ++= Seq("-feature", "-deprecation")

//resolvers += ("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/").withAllowInsecureProtocol(true)

libraryDependencies ++= Seq(
  "com.outr" %% "scribe" % "2.7.10",
  "com.lihaoyi" %% "scalatags" % "0.8.2",
  "com.rometools" % "rome" % "1.8.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
//  "io.monix" %% "monix" % "3.1.0",
  "joda-time" % "joda-time" % "2.9.9",
  "org.json4s" %% "json4s-native" % "3.7.0-M2",
  "org.jsoup" % "jsoup" % "1.10.3",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "com.typesafe.akka" %% "akka-actor" % "2.6.3",
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "com.typesafe.akka" %% "akka-stream" % "2.6.3",
  "org.apache.httpcomponents" % "httpclient" % "4.5.11"
)