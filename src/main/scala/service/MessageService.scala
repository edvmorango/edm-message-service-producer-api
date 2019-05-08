package service

import java.time.LocalDateTime

import domain.{MessageSent, SendMessage}
import scalaz.zio.ZIO
import service.Environment.MessageServiceEnvironment

trait MessageService[R] {

  def publishMessage(message: SendMessage): ZIO[R, Throwable, MessageSent]

}

object MessageServiceImpl extends MessageService[MessageServiceEnvironment] {
  def publishMessage(message: SendMessage)
    : ZIO[MessageServiceEnvironment, Throwable, MessageSent] =
    ZIO.accessM[MessageServiceEnvironment] { env =>
      for {
        _ <- info("Verifying users e-mails.")
        usersOpt <- env.userClient
          .findByEmail(message.senderEmail) zipPar env.userClient.findByEmail(
          message.peerEmail)

        users <- usersOpt match {
          case (Some(s), Some(p)) =>
            info("Users found") *> ZIO.apply((s, p))
          case _ =>
            info("Couldn't find usres") *> ZIO.fail(
              new Exception("Couldn't find users"))
        }
        uuid <- env.UUIDEffect.genUUID()
        time <- ZIO.effectTotal(LocalDateTime.now)
        messageEvent = MessageSent.apply(uuid,
                                         message.message,
                                         users._1,
                                         users._2,
                                         time)
        _ <- env.MessagePub.publishMessage(messageEvent)
        _ <- info("Message published successfully")
      } yield messageEvent

    }
}
