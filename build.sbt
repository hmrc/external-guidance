/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import play.sbt.routes.RoutesKeys

val appName = "external-guidance"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s",
      "-Wconf:cat=unused-imports&src=html/.*:s"
    ),
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(RoutesKeys.routesImport ++= Seq("models._", "models.LabelledDataId._"))


lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies

coverageExcludedPackages := "migrate.*;" +
  "controllers\\.javascript.*;" +
  "testOnly\\..*;" +
  "controllers\\.Reverse.*;" +
  "Routes.*;" +
  "testOnlyDoNotUseInAppConf.*;" +
  "prod;" +
  "repositories;" +
  "app"
coverageHighlighting := true
coverageFailOnMinimum := false
coverageMinimumStmtTotal := 94.7
coverageMinimumBranchTotal := 90
