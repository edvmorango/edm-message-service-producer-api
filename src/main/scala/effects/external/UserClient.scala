package effects.external

import com.softwaremill.sttp.asynchttpclient.zio.AsyncHttpClientZioBackend
import config.UserServiceConfig
import domain.User
import scalaz.zio.{UIO, ZIO}

trait UserClient {

  val userClient: UserClient.Service

}

object UserClient {

  trait Service {

    def findByEmail(email: String): UIO[Option[User]]

  }

}

class UserClientSTTP(userServiceConfig: UserServiceConfig)
    extends UserClient.Service {

  import com.softwaremill.sttp._
  import com.softwaremill.sttp.circe._
  import io.circe.generic.auto._

  implicit val sttpBackend: SttpBackend[ZIO[Any, Throwable, ?], Nothing] =
    AsyncHttpClientZioBackend()

  private def parse[R](
      resp: Response[Either[DeserializationError[io.circe.Error], R]]) = {
    if (resp.is200)
      ZIO
        .fromEither(resp.unsafeBody)
        .mapError(_ => new Exception(s"Invalid response: ${resp.rawErrorBody}"))
    else
      ZIO.fail(new Exception(s"Invalid response: ${resp.rawErrorBody}"))

  }

  def findByEmail(email: String): UIO[Option[User]] = {
    val r = for {
      response <- sttp
        .get(uri"${userServiceConfig.baseUri}/user?email=$email")
        .response(asJson[User])
        .send()
      result <- parse(response)
    } yield result

    r.either.map {
      case Right(res) => Some(res)
      case _          => None
    }

  }

}
