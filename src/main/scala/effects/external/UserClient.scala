package effects.external

import com.softwaremill.sttp.SttpBackend
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

class UserClientSTTP(userServiceConfig: UserServiceConfig)(
    implicit val sttpBackend: SttpBackend[ZIO[Any, Throwable, ?], Nothing])
    extends UserClient.Service {

  import com.softwaremill.sttp._
  import com.softwaremill.sttp.circe._
  import io.circe.generic.auto._

  private def parse[R](
      resp: Response[Either[DeserializationError[io.circe.Error], R]]) = {
    if (resp.is200) {
      resp.unsafeBody match {
        case Right(r) => ZIO.succeed(r)
        case _        => ZIO.dieMessage(s"Couldn't parse: ${resp.rawErrorBody}")
      }
    } else
      ZIO.dieMessage(s"Invalid response: $resp")

  }

  def findByEmail(email: String): UIO[Option[User]] = {
    val r = for {
      response <- sttp
        .get(uri"${userServiceConfig.baseUri}/user?email=$email")
        .response(asJson[User])
        .send()
      result <- parse(response)
    } yield result

    r.either.map(_.toOption)

  }

}
