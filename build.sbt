name := "free-monad-coproduct-example"

version := "1.0"

scalaVersion := "2.12.2"

scalacOptions := Seq("-Ypartial-unification")

resolvers += Resolver.bintrayRepo("projectseptemberinc", "maven")

libraryDependencies ++= {
  Seq(
    "org.typelevel"             %% "cats"     % "0.9.0",
    "com.projectseptember"      %% "freek"    % "0.6.7"
  )
}
