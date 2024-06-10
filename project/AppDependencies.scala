import sbt.*

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % "8.6.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % "2.0.0",
    "uk.gov.hmrc"       %% "auth-client"               % "8.0.0",
    "org.typelevel"     %% "cats-core"                 % "2.12.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalamock"                %% "scalamock"               % "5.2.0"  % Test,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-test-play-30" % "2.0.0" % Test,
    "org.scalatestplus.play"       %% "scalatestplus-play"      % "7.0.1"  % Test,
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30"  % "8.6.0" % Test
  )
}
