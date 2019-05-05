package effects

import com.softwaremill.sttp.asynchttpclient.zio.AsyncHttpClientZioBackend
import config.UserServiceConfig
import domain.User
import scalaz.zio.ZIO

trait UserClient {

  val userClient: UserClient.Service

}

object UserClient {

  trait Service {

    def findByEmail(email: String): ZIO[Any, Throwable, Option[User]]

  }

}

class UserClientSTTP(userServiceConfig: UserServiceConfig)
    extends UserClient.Service {

  import com.softwaremill.sttp._
  import com.softwaremill.sttp.circe._
  import io.circe.generic.auto._

  implicit val sttpBackend = AsyncHttpClientZioBackend()

  private def parse[R](
      resp: Response[Either[DeserializationError[io.circe.Error], R]]) = {
    if (resp.is200)
      ZIO
        .fromEither(resp.unsafeBody)
        .mapError(e => new RuntimeException(e.message))
    else
      ZIO.fail(new Exception(s"Invalid response: ${resp.rawErrorBody}"))

  }

  def findByEmail(email: String): ZIO[Any, Throwable, Option[User]] =
    for {
      response <- sttp
        .get(uri"${userServiceConfig.baseUri}/user?email=$email")
        .response(asJson[User])
        .send()
      parsed <- parse(response).either
      result <- parsed match {
        case Right(value) => ZIO.succeed(Option(value))
        case _            => ZIO(None)

      }
    } yield result

}
