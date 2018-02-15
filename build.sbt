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
    "com.typesafe.akka"  %% "akka-stream"                 % "2.5.8",
    "com.typesafe.akka"  %% "akka-http-spray-json"        % "10.1.0-RC2",
    "com.google.inject"  %  "guice"                       % "4.1.0",

    "org.springframework.cloud" % "spring-cloud-starter-consul-all" % "1.3.2.RELEASE",,
    "org.springframework.cloud" % "spring-cloud-consul-core" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-consul-binder" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-consul-config" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-consul-discovery" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-starter-consul" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-starter-consul-bus" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-starter-consul-config" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-starter-consul-discovery" % "1.3.2.RELEASE",
    "org.springframework.cloud" % "spring-cloud-starter-consul-all" % "1.3.2.RELEASE"

  )
}

resolvers += Resolver.mavenLocal