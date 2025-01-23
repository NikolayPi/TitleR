package com.np96.titler

import cats.effect.IO
import com.np96.titler.TitlerRoutes.TitlesRequest
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.jsonEncoderOf
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits._

class TitlesSpec extends CatsEffectSuite {

  implicit val encoder: Encoder[TitlesRequest] = deriveEncoder[TitlesRequest]
  implicit val entityEncoder: EntityEncoder[IO, TitlesRequest] =
    jsonEncoderOf

  val ignore: Unit = StubServer.run[IO].unsafeRunAndForget()

  private[this] def restTitleRoute(): IO[HttpRoutes[IO]] = {

    val routes = for {
      client <- EmberClientBuilder.default[IO].build

      titles = Titles.impl[IO](client)
      routes = TitlerRoutes.titleRoutes[IO](titles)
    } yield routes

    routes.use(r => IO(r))
  }

  private def runTest(url: String): IO[String] = {
    for {
      route <- restTitleRoute()
      response <- route.orNotFound.run(titleRequest(List(url)))
      stringResponse <- response.as[String]
    } yield stringResponse
  }

  private def runIncorrectRequestTest(): IO[String] = {
    for {
      route <- restTitleRoute()
      response <- route.orNotFound.run(incorrectTitleRequest())
      stringResponse <- response.as[String]
    } yield stringResponse
  }

  def titleRequest(uris: List[String]): Request[IO] =
    Request[IO](Method.GET, uri"/titles").withEntity[TitlesRequest](TitlesRequest(urls = uris))

  def incorrectTitleRequest(): Request[IO] =
    Request[IO](Method.GET, uri"/titles").withEntity[String]("""{"wrong": "json"}""")

  test("Titles handles missing title correctly") {

    assertIO(
      runTest("http://localhost:8099/missing"),
      """[{"TitleResponse":{"url":"http://localhost:8099/missing","result":{"title":""}}}]"""
    )
  }

  test("Titles handles blank page correctly") {
    assertIO(
      runTest("http://localhost:8099/blank"),
      """[{"TitleResponse":{"url":"http://localhost:8099/blank","result":{"title":""}}}]"""
    )
  }

  test("Titles handles empty page correctly") {
    assertIO(
      runTest("http://localhost:8099/empty"),
      """[{"TitleResponse":{"url":"http://localhost:8099/empty","result":{"title":""}}}]"""
    )
  }

  test("Titles handles simple page correctly") {
    assertIO(
      runTest("http://localhost:8099/simple"),
      """[{"TitleResponse":{"url":"http://localhost:8099/simple","result":{"title":"SomeTitle"}}}]"""
    )
  }

  test("Titles handles corner case correctly") {
    assertIO(
      runTest("http://localhost:8099/corner-case"),
      """[{"TitleResponse":{"url":"http://localhost:8099/corner-case","result":{"title":""}}}]"""
    )
  }

  test("Titles handles random internet page") {
    assertIO(
      runTest("https://http4s.org/"),
      """[{"TitleResponse":{"url":"https://http4s.org/","result":{"title":"http4s"}}}]"""
    )
  }

  test("Titles handles missing internet page gracefully") {
    assertIO(
      runTest("https://failing-page.orgggg/"),
      """[{"CrawlerError":{"url":"https://failing-page.orgggg/","error":"failing-page.orgggg: nodename nor servname provided, or not known"}}]"""
    )
  }

  test("Titles handles wrong url gracefully") {
    assertIO(
      runTest("1234:r::$sresdfailingds-page.org/"),
      """[{"CrawlerError":{"url":"1234:r::$sresdfailingds-page.org/","error":"Invalid URL: 1234:r::$sresdfailingds-page.org/"}}]"""
    )
  }

  test("Titles handles incorrect request gracefully") {
    assertIO(
      runIncorrectRequestTest(),
      """{"error":"Invalid message body: Could not decode JSON: {\n  \"wrong\" : \"json\"\n}"}"""
    )
  }

}
