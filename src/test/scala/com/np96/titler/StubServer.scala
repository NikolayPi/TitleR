package com.np96.titler

import cats.effect.Async
import com.comcast.ip4s._
import fs2.io.net.Network
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._

object StubServer {

  def run[F[_]: Async: Network]: F[Nothing] = {
    import org.http4s.dsl.Http4sDsl

    val dsl = new Http4sDsl[F] {}
    import dsl._

    val routes = HttpRoutes.of[F] {
      case GET -> Root / "blank" =>
        Ok("")
      case GET -> Root / "missing" =>
        Ok("<html></html>")
      case GET -> Root / "empty" =>
        Ok("<html><head><title></title></head></html>")
      case GET -> Root / "simple" =>
        Ok("<html><head><title>SomeTitle</title></head></html>")
      case GET -> Root / "corner-case" =>
        Ok(
          "<html><head></head><body>\"We will be talking about <title> and </title>\"</body></html>"
        )
      case GET -> Root / "broken-html" =>
            Ok(
              "<html><htm>/</htm>?/<head><<>><title>asdasdas</title>"
            )
    }
    val app = routes.orNotFound

    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8099")
      .withHttpApp(app)
      .build
      .useForever
  }
}
