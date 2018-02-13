name := "akka-quickstart-scala"

version := "1.1"

scalaVersion := "2.12.2"

lazy val akkaVersion = "2.5.3"

libraryDependencies ++= {
  val akkaHttpV      = "10.0.+"

  Seq(
    "com.typesafe.akka"  %% "akka-actor"                  % akkaVersion,
    "com.typesafe.akka"  %% "akka-testkit"                % akkaVersion,

    "org.scalatest"      %% "scalatest"                   % "3.0.1" % "test",
    "com.typesafe.akka"  %% "akka-http"                   % akkaHttpV,
    "com.typesafe.akka"  %% "akka-http-testkit"           % akkaHttpV % "test",
    "com.typesafe.akka"  %% "akka-stream"                 % "2.5.8"
  )
}
