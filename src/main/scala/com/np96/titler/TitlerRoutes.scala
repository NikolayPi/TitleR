package com.np96.titler

import cats.effect.Concurrent
import cats.implicits._
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

object TitlerRoutes {

  case class TitlesRequest(urls: List[String])
  private case class ServerError(error: String)

  def titleRoutes[F[_]: Concurrent](T: Titles[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    implicit val decoder: EntityDecoder[F, TitlesRequest] =
      jsonOf[F, TitlesRequest]

    implicit val serverErrorEnc: Encoder[ServerError] = deriveEncoder[ServerError]
    implicit def serverErrorEncoder: EntityEncoder[F, ServerError] =
      jsonEncoderOf

    HttpRoutes.of[F] { case req @ GET -> Root / "titles" =>
      (for {
        urls <- req.as[TitlesRequest].map(_.urls)

        resp <- Ok(T.get(urls))
      } yield resp).handleErrorWith(t => Ok(ServerError(t.getMessage))) // handle payload parse error
    }
  }

}
