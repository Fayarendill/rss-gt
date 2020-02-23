name := "rssFeednGT"

version := "0.1"

scalaVersion := "2.13.1"

scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= Seq(
  "com.outr" %% "scribe" % "2.7.10",
//  "com.outr" %% "scribe-slf4j" % "1.4.3",
//  "tv.cntt" %% "xitrum" % "3.28.2",
  "com.lihaoyi" %% "scalatags" % "0.8.2",
  "com.rometools" % "rome" % "1.8.0",
  "io.monix" %% "monix" % "3.1.0",
  "joda-time" % "joda-time" % "2.9.9",
  "org.json4s" %% "json4s-native" % "3.7.0-M2",
  "org.jsoup" % "jsoup" % "1.10.3",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
//  "org.scalafx" %% "scalafx" % "8.0.102-R11",
)