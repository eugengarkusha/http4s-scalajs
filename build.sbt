import Dependencies.Versions
import sbt.addCompilerPlugin
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

inThisBuild(
  List(
    scalaVersion := Dependencies.Versions.scala,
    organization := "com.example",
    scalacOptions := Seq(
      "-encoding",
      "UTF-8",
      "-Ypartial-unification",
      "-deprecation" //, // warning and location for usages of deprecated APIs
      // "-feature", // warning and location for usages of features that should be imported explicitly
      // "-unchecked", // additional warnings where generated code depends on assumptions
      // "-Xlint", // recommended additional warnings
      // "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
      //"-Ywarn-inaccessible",
      //"-Ywarn-dead-code"
    )
  ))

lazy val compilerPlugins = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  addCompilerPlugin(Dependencies.`kind-projector` cross CrossVersion.binary),
  //need this for circe
  addCompilerPlugin(Dependencies.paradise cross CrossVersion.full)
)

lazy val shared = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.`circe-generic`.value,
      Dependencies.`circe-java8`.value,
      Dependencies.`cats-effect`.value,
    ) ++ Dependencies.tsecAll,
    compilerPlugins
  )
  .jsConfigure(_ enablePlugins ScalaJSWeb)
  .jsSettings(
    libraryDependencies ++= Def
      .setting(
        Seq(
          Dependencies.`scala-java-time`.value
        ))
      .value
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb, ScalaJSBundlerPlugin)
  .settings(
    compilerPlugins,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Def
      .setting(Seq(
        Dependencies.`cats-core`.value,
        Dependencies.`monocle-macro`.value,
        Dependencies.`monocle-core`.value,
        Dependencies.`scalajs-react-core`.value,
        Dependencies.`scalajs-react-extra`.value,
        Dependencies.`scalajs-ext-cats`.value,
        Dependencies.`scalajs-monocle`.value,
        Dependencies.`scalacss-ext-react`.value,
        Dependencies.`scalajs-dom`.value,
        Dependencies.`circe-generic`.value,
        Dependencies.`circe-parser`.value,
        Dependencies.`circe-java8`.value
      ) ++ Dependencies.tsecAll)
      .value,
    npmDependencies in Compile ++= Seq(
      Dependencies.react,
      Dependencies.`react-dom`
    ) //,
//  emitSourceMaps := false,
//  jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
  )
  .dependsOn(sharedJs)

lazy val server = (project in file("server"))
  .enablePlugins(WebScalaJSBundlerPlugin)
  .settings(
    compilerPlugins,
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      Dependencies.zio.value,
      Dependencies.`circe-generic`.value,
      Dependencies.`circe-java8`.value,
      Dependencies.`http4s-core`,
      Dependencies.`http4s-circe`,
      Dependencies.`http4s-dsl`,
      Dependencies.scalatags.value,
      Dependencies.`http4s-blaze-server`.value,
      "org.reactormonk" %% "cryptobits" % "1.1"
    ) ++ Dependencies.tsecAll,
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value
    // Compile the project before generating Eclipse files, so that generated .scala or .class files for Twirl templates are present
  )
  .dependsOn(sharedJvm)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen { s: State =>
  "project server" :: s
}
