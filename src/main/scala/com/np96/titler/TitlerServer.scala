package com.np96.titler

import cats.Parallel
import cats.effect.Async
import com.comcast.ip4s._
import com.np96.titler.AppConfig.loadConfig
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object TitlerServer {

  def run[F[_]: Parallel: Async: Network]: F[Nothing] = {
    for {
      client <- EmberClientBuilder.default[F].build
      titleAlg = Titles.impl[F](client)

      httpApp = TitlerRoutes.titleRoutes[F](titleAlg).orNotFound

      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

      appConfig = loadConfig()

      _ <-
        EmberServerBuilder
          .default[F]
          .withHost(Host.fromString(appConfig.host).get)
          .withPort(Port.fromInt(appConfig.port).get)
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
