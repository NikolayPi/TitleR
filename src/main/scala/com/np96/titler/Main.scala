package com.np96.titler

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run: IO[Nothing] = TitlerServer.run[IO]
}
