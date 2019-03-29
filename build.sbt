import com.typesafe.sbt.pgp.PgpKeys
import sbt.Keys._

name := "scalac-scapegoat-plugin"

organization := "com.sksamuel.scapegoat"

scalaVersion := "2.12.4"
crossScalaVersions := Seq("2.11.12", scalaVersion.value)

sbtVersion in Global := "1.1.0"
crossSbtVersions := Vector("0.13.16", sbtVersion.value)

SbtPgp.autoImport.useGpg := true

SbtPgp.autoImport.useGpgAgent := true

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

fullClasspath in console in Compile ++= (fullClasspath in Test).value // because that's where "PluginRunner" is

initialCommands in console := s"""
import com.sksamuel.scapegoat._
def check(code: String) = {
  val runner = new PluginRunner { val inspections = ScapegoatConfig.inspections }
  // Not sufficient for reuse, not sure why.
  // runner.reporter.reset
  val c = runner compileCodeSnippet code
  val feedback = c.scapegoat.feedback
  feedback.warnings map (x => "%-40s  %s".format(x.text, x.snippet getOrElse "")) foreach println
  feedback
}
"""

scalacOptions ++= Seq(
  "-Xlint",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen"
  //"-Ywarn-value-discard"
)
  
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

libraryDependencies ++= Seq(
  "org.scala-lang"                  %     "scala-reflect"         % scalaVersion.value,
  "org.scala-lang"                  %     "scala-compiler"        % scalaVersion.value      % "provided",
  "org.scala-lang.modules"          %%    "scala-xml"             % "1.0.6",
  "org.scala-lang"                  %     "scala-compiler"        % scalaVersion.value      % "test",
  "commons-io"                      %     "commons-io"            % "2.5"                   % "test",
  "org.scalatest"                   %%    "scalatest"             % "3.0.4"                 % "test",
  "org.mockito"                     %     "mockito-all"           % "1.10.19"               % "test",
  "joda-time"                       %     "joda-time"             % "2.9.9"                 % "test",
  "org.joda"                        %     "joda-convert"          % "1.9.2"                 % "test",
  "org.slf4j"                       %     "slf4j-api"             % "1.7.25"                % "test"
)

sbtrelease.ReleasePlugin.autoImport.releasePublishArtifactsAction := PgpKeys.publishSigned.value

sbtrelease.ReleasePlugin.autoImport.releaseCrossBuild := true

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

publishMavenStyle := true

publishArtifact in Test := false

parallelExecution in Test := false

pomIncludeRepository := {
  _ => false
}

pomExtra := {
  <url>https://github.com/sksamuel/scapegoat</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:sksamuel/scapegoat.git</url>
      <connection>scm:git@github.com:sksamuel/scapegoat.git</connection>
    </scm>
    <developers>
      <developer>
        <id>sksamuel</id>
        <name>sksamuel</name>
        <url>http://github.com/sksamuel</url>
      </developer>
    </developers>
}
