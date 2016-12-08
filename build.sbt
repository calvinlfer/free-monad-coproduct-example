name := "free-monad-coproduct-example"

version := "1.0"

scalaVersion := "2.11.8"

lazy val `free-monad-coproduct-example` =
  (project in file("."))
    .settings(Seq(addCompilerPlugin("com.milessabin" % "si2712fix-plugin_2.11.8" % "1.2.0")))

resolvers += Resolver.jcenterRepo

libraryDependencies ++= {
  Seq(
    "org.typelevel"             %% "cats"     % "0.8.1",
    "com.projectseptember"      %% "freek"    % "0.6.5"
  )
}