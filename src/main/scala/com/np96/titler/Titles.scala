package com.np96.titler

import cats.Parallel
import cats.effect.Concurrent
import cats.implicits._
import io.circe.Encoder
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe._
import org.http4s.Method.GET
import org.jsoup.Jsoup

sealed trait Titles[F[_]] {
  def get(urls: Seq[String]): F[Seq[Titles.CrawlerResponse]]
}

object Titles {
  def apply[F[_]](implicit ev: Titles[F]): Titles[F] = ev

  final case class Title(title: String) extends AnyVal

  private[titler] object Title {

    implicit val titleEncoder: Encoder[Title] = deriveEncoder[Title]
    implicit def titleEntityEncoder[F[_]]: EntityEncoder[F, Title] =
      jsonEncoderOf

    def fromHtmlString(html: String): Title = {
      val doc = Jsoup.parse(html)
      Title(doc.title())
    }
  }

  sealed trait CrawlerResponse

  final case class TitleResponse(url: String, result: Title) extends CrawlerResponse
  final case class CrawlerError(url: String, error: String) extends CrawlerResponse

  implicit val titleResponseEncoder: Encoder[TitleResponse] = deriveEncoder[TitleResponse]
  implicit val crawlerErrorEncoder: Encoder[CrawlerError] = deriveEncoder[CrawlerError]
  implicit val crawlerResponseEncoder: Encoder[CrawlerResponse] = deriveEncoder[CrawlerResponse]

  implicit def titleResponseEntityEncoder[F[_]]: EntityEncoder[F, TitleResponse] =
    jsonEncoderOf

  implicit def crawlerErrorEncoder[F[_]]: EntityEncoder[F, CrawlerError] =
    jsonEncoderOf

  implicit def crawlerResponseEncoder[F[_]]: EntityEncoder[F, CrawlerResponse] =
    jsonEncoderOf

  implicit def crawlerSeqResponseEncoder[F[_]]: EntityEncoder[F, Seq[CrawlerResponse]] =
    jsonEncoderOf

  def impl[F[_]: Parallel: Concurrent](C: Client[F]): Titles[F] = new Titles[F] {
    private val dsl = new Http4sClientDsl[F] {}
    import dsl._

    override def get(urls: Seq[String]): F[Seq[Titles.CrawlerResponse]] = {
      Parallel.parTraverse(urls)(get)
    }

    def get(url: String): F[Titles.CrawlerResponse] = {

      val parsed = Uri.fromString(url)

      for {
        uri <-
          if (parsed.isRight) Concurrent[F].pure(parsed.toOption.get)
          else Concurrent[F].raiseError(new IllegalArgumentException(s"Invalid URL: ${url}"))

        resp <- C
          .expect[String](GET(uri))
          .map(s => TitleResponse(url, Title.fromHtmlString(s))): F[CrawlerResponse]

      } yield resp
    }.recover { case t =>
      // handle URI parse failure
      CrawlerError(url, t.getMessage)
    }
  }
}
