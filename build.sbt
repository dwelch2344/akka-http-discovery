name := "akka-quickstart-scala"
organization := "example"
version := "1.1"

scalaVersion := "2.12.2"

libraryDependencies ++= {
  val akkaHttpV      = "10.0.+"
  val akkaVersion = "2.5.3"

  Seq(
    "com.typesafe.akka"  %% "akka-actor"                  % akkaVersion,
    "com.typesafe.akka"  %% "akka-testkit"                % akkaVersion,

    "org.scalatest"      %% "scalatest"                   % "3.0.1" % "test",
    "com.typesafe.akka"  %% "akka-http"                   % akkaHttpV,
    "com.typesafe.akka"  %% "akka-http-testkit"           % akkaHttpV % "test",
    "com.typesafe.akka"  %% "akka-stream"                 % "2.5.8",
    "com.typesafe.akka"  %% "akka-http-spray-json"        % "10.1.0-RC2",
    "com.google.inject"  %  "guice"                       % "4.1.0",

    "org.scalamock" %% "scalamock" % "4.0.0" % Test,

    "org.springframework.cloud" % "spring-cloud-starter-consul-all" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-consul-core" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-consul-binder" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-consul-config" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-consul-discovery" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-starter-consul" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-starter-consul-bus" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-starter-consul-config" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-starter-consul-discovery" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-starter-consul-all" % "1.3.2.RELEASE",

    "io.zipkin.brave" % "brave" % "4.16.2",
    "io.zipkin.reporter2" % "zipkin-sender-okhttp3" % "2.3.3"

  )
}

resolvers += Resolver.mavenLocal
Defaults.itSettings
Revolver.settings

enablePlugins(JavaAppPackaging, JavaAgent)

// when you call "sbt run" aspectj weaving kicks in
//javaAgents += "org.aspectj" % "aspectjweaver" % "1.8.10"
//javaOptions in Universal += "-Dorg.aspectj.tracing.factory=default"
fork in run := true

initialCommands := """|import akka.actor._
                      |import akka.pattern._
                      |import akka.util._
                      |import scala.concurrent._
                      |import scala.concurrent.duration._""".stripMargin