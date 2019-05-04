package effects

import config.UserServiceConfig
import domain.User
import scalaz.zio.ZIO

trait UserClient {

  def UserClientEffect: UserClient.Effect[Any]

}

object UserClient {

  trait Effect[R] {

    def findByEmail(email: String): ZIO[R, Throwable, Option[User]]

  }

}

final class UserClientSttp(userServiceConfig: UserServiceConfig)
    extends UserClient.Effect[Sttp] {

  import com.softwaremill.sttp._
  import com.softwaremill.sttp.circe._
  import io.circe.generic.auto._

  private def parse[R](
      resp: Response[Either[DeserializationError[io.circe.Error], R]]) = {
    if (resp.is200)
      ZIO
        .fromEither(resp.unsafeBody)
        .mapError(e => new RuntimeException(e.message))
    else
      ZIO.fail(new Exception(s"Invalid response: ${resp.rawErrorBody}"))

  }

  def findByEmail(email: String): ZIO[Sttp, Throwable, Option[User]] =
    ZIO.accessM[Sttp] { eff =>
      import eff.SttpEffect._

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

}
