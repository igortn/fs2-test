name := "fs2-test"

version := "0.1"

scalaVersion := "2.12.7"

scalacOptions ++= Seq(
	"-language:higherKinds"
)

lazy val fs2Version = "1.0.0"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version
)

cancelable in Global := true