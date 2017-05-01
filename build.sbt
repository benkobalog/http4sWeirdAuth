name := "practice1"

version := "1.0"

scalaVersion := "2.11.8"

val http4sVersion = "0.15.8"

// Only necessary for SNAPSHOT releases
//resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.reactormonk" %% "cryptobits" % "1.1"
)

// Cancellable with ctrl + c in sbt cmd
//fork in run := true
//cancelable in Global := true

val logbackVersion = "1.1.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % logbackVersion