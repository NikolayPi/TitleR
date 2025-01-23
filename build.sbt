val Http4sVersion = "0.23.30"
val CirceVersion = "0.14.10"
val MunitVersion = "1.0.4"
val LogbackVersion = "1.5.16"
val MunitCatsEffectVersion = "2.0.0"
val JsoupVersion = "1.18.1"
val PureConfigVersion = "0.17.8"

lazy val root = (project in file("."))
  .settings(
    organization := "com.np96",
    name := "titler",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.15",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime,
      "org.jsoup" % "jsoup" % JsoupVersion,
      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-core" % PureConfigVersion
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    assembly / assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case x                   => (assembly / assemblyMergeStrategy).value.apply(x)
    }
  )
