
inThisBuild(List(
  scalaVersion := Dependencies.Versions.scala,
  organization := "com.example",
  scalacOptions := Seq(
    "-encoding", "UTF-8",
    "-deprecation"//, // warning and location for usages of deprecated APIs
   // "-feature", // warning and location for usages of features that should be imported explicitly
   // "-unchecked", // additional warnings where generated code depends on assumptions
   // "-Xlint", // recommended additional warnings
   // "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
    //"-Ywarn-inaccessible",
    //"-Ywarn-dead-code"
  )
))


//need this for circe
lazy val macroParadiseSettings = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  addCompilerPlugin(Dependencies.paradise cross CrossVersion.full)
)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.`circe-generic`.value,
      Dependencies.`circe-java8`.value
    ),
    macroParadiseSettings
  )
  .jsConfigure(_ enablePlugins ScalaJSWeb)
  .jsSettings(
    libraryDependencies ++= Def.setting(Seq(
      Dependencies.`scala-java-time`.value
    )).value
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js



lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb, ScalaJSBundlerPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Def.setting(Seq(
    Dependencies.`scalajs-react-core`.value,
    Dependencies.`scalajs-react-extra`.value,
    Dependencies.`scalacss-ext-react`.value,
    Dependencies.`scalajs-dom`.value,
    Dependencies.`circe-generic`.value,
    Dependencies.`circe-parser`.value,
    Dependencies.`circe-java8`.value)
  ).value,
  npmDependencies in Compile ++= Seq(
    Dependencies.react,
    Dependencies.`react-dom`
  )//,
//  emitSourceMaps := false,
//  jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv

).dependsOn(sharedJs)




lazy val server = (project in file("server"))
  .enablePlugins(WebScalaJSBundlerPlugin)
  .settings(
  macroParadiseSettings,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    Dependencies.`circe-generic`.value,
    Dependencies.`circe-java8`.value,
    Dependencies.`http4s-core`.value,
    Dependencies.`http4s-circe`.value,
    Dependencies.`http4s-dsl`.value,
    Dependencies.scalatags.value,
    Dependencies.`http4s-blaze-server`.value
),
  WebKeys.packagePrefix in Assets := "public/",
  managedClasspath in Runtime += (packageBin in Assets).value,
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for Twirl templates are present
  EclipseKeys.preTasks := Seq(compile in Compile)
).dependsOn(sharedJvm)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}
