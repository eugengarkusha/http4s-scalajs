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
    val `kind-projector` = "0.9.6"
    val tsec = "0.0.1-M11"
  }

  val paradise = "org.scalamacros" % "paradise" % Versions.paradise

  val `http4s-core` = "org.http4s" %% "http4s-core" %  Versions.http4s
  val `http4s-circe` = "org.http4s" %% "http4s-circe" % Versions.http4s
  val `http4s-dsl` = "org.http4s" %% "http4s-dsl" %  Versions.http4s
  val `http4s-blaze-server` = Def.setting("org.http4s" %% "http4s-blaze-server" %  Versions.http4s)
  val `tsec-common` = "io.github.jmcardon" %% "tsec-common" % Versions.tsec
  val `tsec-jwt-core` = "io.github.jmcardon" %% "tsec-jwt-core" % Versions.tsec
  val `tsec-jwt-mac` = "io.github.jmcardon" %% "tsec-jwt-mac" % Versions.tsec
  //TODO: SEPARATE
  val tsecAll = Seq(
    "io.github.jmcardon" %% "tsec-common" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-password" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-cipher-jca" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-cipher-bouncy" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-mac" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-signatures" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-hash-jca" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-hash-bouncy" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-libsodium" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-jwt-mac" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-jwt-sig" % Versions.tsec,
    "io.github.jmcardon" %% "tsec-http4s" % Versions.tsec)



  // used to bootstrap js app
  val scalatags = Def.setting("com.lihaoyi" %% "scalatags" % "0.6.7")

  val `cats-effect` =  Def.setting("org.typelevel" %%% "cats-effect" % "0.10")
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

  //compiler plugins
  val `kind-projector` = "org.spire-math" %% "kind-projector" % Versions.`kind-projector`
  //NPM
  val react: NpmDependency = "react" -> Versions.react
  val `react-dom`: NpmDependency = "react-dom" -> Versions.react
}
