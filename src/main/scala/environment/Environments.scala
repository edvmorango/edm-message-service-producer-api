package environment

import config.Config
import effects.{ConsoleLogger, Logger, UUID, ZUUID}
import effects.external.{UserClient, UserClientSTTP}
import effects.publisher.{MessagePublisher, MessagePublisherSNS, SNSClient}
import scalaz.zio.TaskR
import scalaz.zio.blocking.Blocking
import scalaz.zio.clock.Clock
import scalaz.zio.console.Console
import scalaz.zio.random.Random
import scalaz.zio.scheduler.Scheduler
import scalaz.zio.system.System

object Environments {

  type AppEnvironment = Clock
    with Logger
    with UUID
    with UserClient
    with MessagePublisher

  type AppTask[A] = TaskR[AppEnvironment, A]

  type MessageServiceEnvironment = Logger
    with UUID
    with UserClient
    with MessagePublisher

  def createEnvironment(cfg: Config)(
      base: Clock with Console with System with Random with Blocking) = {
    new Clock with Logger with UUID with UserClient with MessagePublisher {

      val clock: Clock.Service[Any] = base.clock
      val scheduler: Scheduler.Service[Any] = base.scheduler

      val userClient: UserClient.Service =
        new UserClientSTTP(cfg.userService)

      val log: Logger.Effect = new ConsoleLogger()

      def UUIDEffect: UUID.Effect = ZUUID

      def MessagePub: MessagePublisher.Effect =
        new MessagePublisherSNS(cfg.events.userMessageEvent,
                                SNSClient.instantiate(cfg.aws.sns))
    }
  }

}
