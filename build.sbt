name := "spiceworks-challenge"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.+"
libraryDependencies += "net.sf.opencsv" % "opencsv" % "2.3"
libraryDependencies += "org.specs2" %% "specs2-core" % "3.0" % "test"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

scalacOptions in Test ++= Seq("-Yrangepos")