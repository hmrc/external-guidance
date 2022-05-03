import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % "5.20.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % "0.63.0",
    "uk.gov.hmrc"       %% "auth-client"               % "5.8.0-play-28"
  )

  val test = Seq(
    "org.scalamock"                %% "scalamock"               % "5.2.0"  % "test",
    "org.scalatest"                %% "scalatest"               % "3.1.4"  % "test",
    "com.typesafe.play"            %% "play-test"               % current  % "test",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-test-play-28" % "0.63.0" % "test",
    "org.pegdown"                  %  "pegdown"                 % "1.6.0"  % "test, it",
    "org.scalatestplus.play"       %% "scalatestplus-play"      % "5.1.0"  % "test, it",
    "com.github.tomakehurst"       %  "wiremock-jre8"           % "2.32.0" % "test, it",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"    % "2.13.2" % "test, it",
    "uk.gov.hmrc"                  %% "bootstrap-test-play-28"  % "5.20.0" % "test, it",
    "com.typesafe.play"            %% "play-akka-http-server"   % "2.8.14"  % "test, it"
  )
}
