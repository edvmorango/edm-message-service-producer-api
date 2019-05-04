package endpoint

import domain.SendMessage
import json.JsonSupportEndpoint
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import scalaz.zio.interop.catz._
import scalaz.zio.{TaskR, ZIO}
import io.circe.generic.auto._

final class MessageEndpoint[R <: Any](rootUri: String)
    extends JsonSupportEndpoint[R] {

  type MessageTask[A] = TaskR[R, A]

  val dsl: Http4sDsl[MessageTask] = Http4sDsl[MessageTask]

  import dsl._

  def endpoints: HttpRoutes[MessageTask] =
    HttpRoutes.of[MessageTask] {

      case req @ POST -> Root / `rootUri` =>
        val sendMessage: ZIO[R, Throwable, SendMessage] =
          req.as[SendMessage]

        Created(sendMessage)

    }
}
