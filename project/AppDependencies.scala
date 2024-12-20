import sbt.*

object AppDependencies {

  val bootstrapVersion = "9.5.0"
  val mongoPlayVersion = "2.3.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % mongoPlayVersion,
    "uk.gov.hmrc"       %% "auth-client"               % "8.2.0",
    "org.typelevel"     %% "cats-core"                 % "2.12.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalamock"          %% "scalamock"               % "6.0.0",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % mongoPlayVersion,
    "org.scalatestplus.play" %% "scalatestplus-play"      % "7.0.1",
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapVersion
  ).map(_ % Test)
}
