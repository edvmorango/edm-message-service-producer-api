package service

import java.time.LocalDateTime

import domain.{MessageSent, SendMessage, User}
import environment.Environments.MessageServiceEnvironment
import scalaz.zio.ZIO

trait MessageService[R] {

  def publishMessage(message: SendMessage): ZIO[R, Throwable, MessageSent]

}

object MessageServiceImpl extends MessageService[MessageServiceEnvironment] {

  private type Task[A] = ZIO[MessageServiceEnvironment, Throwable, A]

  def publishMessage(message: SendMessage)
    : ZIO[MessageServiceEnvironment, Throwable, MessageSent] = {
    ZIO.accessM[MessageServiceEnvironment] { env =>
      def validateEmail(message: SendMessage): Task[Unit] =
        if (message.senderEmail == message.peerEmail)
          ZIO.fail(new Exception("User can't send message to himself"))
        else
          ZIO.unit

      def getUsersPar(
          message: SendMessage): Task[(Option[User], Option[User])] =
        env.userClient
          .findByEmail(message.senderEmail) zipPar env.userClient.findByEmail(
          message.peerEmail)

      def unwrapUsers(users: (Option[User], Option[User])): Task[(User, User)] =
        users match {
          case (Some(s), Some(p)) =>
            info("Users found") *> ZIO.apply((s, p))
          case _ =>
            info("Couldn't find usres") *> ZIO.fail(
              new Exception("Couldn't find users"))
        }

      for {
        _ <- info("Verifying users e-mails.")
        _ <- validateEmail(message)
        users <- getUsersPar(message) >>= unwrapUsers
        uuid <- env.UUIDEffect.genUUID()
        time <- ZIO.effectTotal(LocalDateTime.now)
        messageEvent = MessageSent(uuid,
                                   message.message,
                                   users._1,
                                   users._2,
                                   time)
        _ <- env.MessagePub.publishMessage(messageEvent)
        _ <- info("Message published successfully")
      } yield messageEvent

    }

  }
}
