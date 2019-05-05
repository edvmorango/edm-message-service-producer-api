package service

import java.time.LocalDateTime

import domain.{MessageSent, SendMessage}
import effects.UUID
import effects.external.UserClient
import effects.publisher.MessagePublisher
import scalaz.zio.ZIO
import service.MessageAlias.MessageServiceEnvironment

object MessageAlias {

  type MessageServiceEnvironment = UUID with UserClient with MessagePublisher

}

trait MessageService[R] {

  def publishMessage(message: SendMessage): ZIO[R, Throwable, MessageSent]

}

object MessageServiceImpl extends MessageService[MessageServiceEnvironment] {
  def publishMessage(message: SendMessage)
    : ZIO[MessageServiceEnvironment, Throwable, MessageSent] =
    ZIO.accessM[MessageServiceEnvironment] { env =>
      for {
        usersOpt <- env.userClient
          .findByEmail(message.senderEmail) zipPar env.userClient.findByEmail(
          message.peerEmail)

        users <- usersOpt match {
          case (Some(s), Some(p)) => ZIO.apply((s, p))
          case _                  => ZIO.fail(new Exception("Couldn't find users"))
        }
        uuid <- env.UUIDEffect.genUUID
        time <- ZIO.effectTotal(LocalDateTime.now)
        messageEvent = MessageSent.apply(uuid,
                                         message.message,
                                         users._1,
                                         users._2,
                                         time)
        _ <- env.MessagePub.publishMessage(messageEvent)
      } yield messageEvent

    }
}
