name := "filedsl"

version := "1.0"

scalaVersion := "2.10.2"

sbtVersion := "0.12.4"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "1.9.1",
  "junit" % "junit" % "4.11" % "test"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

EclipseKeys.withSource := true
