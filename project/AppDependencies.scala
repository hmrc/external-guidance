import sbt.*

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % "8.5.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % "1.8.0",
    "uk.gov.hmrc"       %% "auth-client"               % "7.1.0"
  )

  val test = Seq(
    "org.scalamock"                %% "scalamock"               % "6.0.0"  % "test",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-test-play-30" % "1.8.0" % "test",
    "org.scalatestplus.play"       %% "scalatestplus-play"      % "7.0.1"  % "test",
    "uk.gov.hmrc"                  %% "bootstrap-test-play-30"  % "8.5.0" % "test"
  )
}
