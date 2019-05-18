// fast development turnaround when using sbt ~reStart
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.3")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"  % "0.6.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.27")

addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.9-0.6")

//formatting plugin (config at .scalafmt.conf)
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.0")

// sbt-web plugin for adding checksum files for web assets
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.4")

// sbt-web plugin for gzip compressing web assets
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

// Module bundler for Scala.js projects that use NPM packages.
addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.11.0")
