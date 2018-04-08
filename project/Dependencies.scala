import org.scalajs.sbtplugin.impl.DependencyBuilders
import sbt._

object Dependencies extends DependencyBuilders{
  type NpmDependency = (String, String)

  object Versions {
    val scala = "2.12.5"

    val http4s = "0.18.7"
    val twirl = "1.3.15"
    val paradise = "2.1.1"
    val scalatest = "3.0.3"
    val circe = "0.9.3"
    val `scala-java-time` = "2.0.0-M12"
    val `scalajs-react` = "1.2.0"
    val scalacss = "0.5.5"
    val `scalajs-dom` = "0.9.5"
    val react = "16.0"
  }

  val paradise = "org.scalamacros" % "paradise" % Versions.paradise

  val `http4s-core` = Def.setting("org.http4s" %% "http4s-core" %  Versions.http4s)
  val `http4s-circe` = Def.setting("org.http4s" %% "http4s-circe" % Versions.http4s)
  val `http4s-dsl` = Def.setting("org.http4s" %% "http4s-dsl" %  Versions.http4s)
  val `http4s-blaze-server` = Def.setting("org.http4s" %% "http4s-blaze-server" %  Versions.http4s)
  //  val `http4s-twirl` = Def.setting("org.http4s" %% "http4s-twirl" %  Versions.http4s)
//  val twirl = Def.setting("com.typesafe.play" %% "twirl-api" %  Versions.twirl)

  // used to bootstrap js app
  val scalatags = Def.setting("com.lihaoyi" %% "scalatags" % "0.6.7")


  val scalatest = Def.setting("org.scalatest" %% "scalatest" % Versions.scalatest)
  val `circe-generic` = Def.setting("io.circe" %%% "circe-generic" % Versions.circe)
  val `circe-java8` = Def.setting("io.circe" %%% "circe-java8" % Versions.circe)
  val `circe-parser` = Def.setting("io.circe" %%% "circe-parser" % Versions.circe)
  // java.time for scalajs
  val `scala-java-time` = Def.setting("io.github.cquiroz" %%% "scala-java-time" % Versions.`scala-java-time`)
  val `scalajs-react-core` = Def.setting("com.github.japgolly.scalajs-react" %%% "core" % Versions.`scalajs-react`)
  val `scalajs-react-extra` = Def.setting("com.github.japgolly.scalajs-react" %%% "extra" % Versions.`scalajs-react`)
  val `scalacss-ext-react` = Def.setting("com.github.japgolly.scalacss" %%% "ext-react" % Versions.scalacss)
  val `scalajs-dom` = Def.setting("org.scala-js" %%% "scalajs-dom" % Versions.`scalajs-dom`)

  val react: NpmDependency = "react" -> Versions.react
  val `react-dom`: NpmDependency = "react-dom" -> Versions.react
}
