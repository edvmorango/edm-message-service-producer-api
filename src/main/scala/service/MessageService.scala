package service

import domain.{SendMessage, User}
import effects.{UUID, UserClient}
import scalaz.zio.ZIO

object MessageAlias {

  type MessageServiceEnvironment = UUID with UserClient

}

trait MessageService[R] {

  def publishMessage(message: SendMessage): ZIO[R, Throwable, Option[User]]

}

object MessageServiceImpl extends MessageService[UUID with UserClient] {
  def publishMessage(message: SendMessage)
    : ZIO[UUID with UserClient, Throwable, Option[User]] =
    ZIO.accessM[UUID with UserClient] { env =>
      for {
        sender <- env.userClient.findByEmail(message.peerEmail)
      } yield sender

    }
}
