name := "OpendoorChallenge"

version := "1.0"

scalaVersion := "2.10.6"

lazy val versions = new {
  val finatra = "2.1.3"
  val guice = "4.0"
  val logback = "1.0.13"
  val kantanCsv = "0.1.8"
  val geoJson = "1.3.1"
  val postgres = "0.1.0"
}

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

libraryDependencies ++= Seq(
  "com.twitter.finatra" %% "finatra-http" % versions.finatra,
  "com.twitter.finatra" %% "finatra-httpclient" % versions.finatra,
  "com.twitter.finatra" %% "finatra-slf4j" % versions.finatra,
  "com.twitter.inject" %% "inject-core" % versions.finatra,
  "ch.qos.logback" % "logback-classic" % versions.logback,

  "com.nrinaudo" %% "kantan.csv" % versions.kantanCsv,
  "com.nrinaudo" %% "kantan.csv-scalaz" % versions.kantanCsv,
  "com.nrinaudo" %% "kantan.csv-scalaz-stream" % versions.kantanCsv,
  "com.nrinaudo" %% "kantan.csv-cats" % versions.kantanCsv,
  "com.nrinaudo" %% "kantan.csv-generic" % versions.kantanCsv,

  "com.typesafe.play" % "play-json_2.10" % "2.4.6",
  "com.typesafe.play.extras" %% "play-geojson" % versions.geoJson,

  "com.github.finagle" %% "finagle-postgres" % versions.postgres,

  "com.twitter" %% "finagle-http" % "6.33.0",
  "com.twitter" %% "twitter-server" % "1.18.0"
)

enablePlugins(JavaAppPackaging)
