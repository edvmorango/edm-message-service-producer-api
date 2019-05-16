package service

import java.time.LocalDateTime

import domain.{MessageSent, SendMessage, User}
import environment.Environments.MessageServiceEnvironment
import failures.{
  CannotFindUser,
  CannotPublishMessage,
  MessageError,
  UserCannotSendMessageToHimself
}
import scalaz.zio.ZIO

trait MessageService[R] {

  def publishMessage(message: SendMessage): ZIO[R, MessageError, MessageSent]

}

object MessageServiceImpl extends MessageService[MessageServiceEnvironment] {

  private type SenderPeer = (User, User)

  case class Dispatch(sender: User, peer: User)

  def publishMessage(message: SendMessage)
    : ZIO[MessageServiceEnvironment, MessageError, MessageSent] = {

    def validateEmail(message: SendMessage)
      : ZIO[MessageServiceEnvironment, UserCannotSendMessageToHimself, Unit] =
      if (message.senderEmail == message.peerEmail)
        ZIO.fail(UserCannotSendMessageToHimself(message.senderEmail))
      else
        ZIO.unit

    ZIO.accessM[MessageServiceEnvironment] { env =>
      def getUserByEmail(
          email: String): ZIO[MessageServiceEnvironment, CannotFindUser, User] =
        env.userClient
          .findByEmail(email)
          .flatMap {
            case Some(user) => ZIO.succeedLazy(user)
            case None       => ZIO.fail(CannotFindUser(email))
          }
          .orDie

      def getUsersPar(message: SendMessage)
        : ZIO[MessageServiceEnvironment, CannotFindUser, SenderPeer] =
        getUserByEmail(message.senderEmail) <&> getUserByEmail(
          message.peerEmail)

      for {
        _ <- info("Verifying users e-mails.")
        _ <- validateEmail(message)
        users <- getUsersPar(message)
        uuid <- env.UUIDEffect.genUUID()
        time <- ZIO.effectTotal(LocalDateTime.now).orDie
        messageEvent = MessageSent(uuid,
                                   message.message,
                                   users._1,
                                   users._2,
                                   time)
        _ <- env.MessagePub
          .publishMessage(messageEvent)
          .mapError(_ => CannotPublishMessage())
        _ <- info("Message published successfully")
      } yield messageEvent

    }

  }
}
