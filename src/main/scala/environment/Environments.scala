package environment

import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.zio.AsyncHttpClientZioBackend
import config.Config
import effects.external.{UserClient, UserClientSTTP}
import effects.publisher.{MessagePublisher, MessagePublisherSNS}
import effects.{ConsoleLogger, Logger, UUID, ZUUID}
import scalaz.zio.blocking.Blocking
import scalaz.zio.clock.Clock
import scalaz.zio.console.Console
import scalaz.zio.random.Random
import scalaz.zio.scheduler.Scheduler
import scalaz.zio.system.System
import scalaz.zio.{TaskR, ZIO}
import software.amazon.awssdk.services.sns.SnsAsyncClient

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

  def createEnvironment(cfg: Config, snsInstance: SnsAsyncClient)(
      base: Clock with Console with System with Random with Blocking) = {
    new Clock with Logger with UUID with UserClient with MessagePublisher {

      implicit val sns = snsInstance

      implicit val sttpBackend: SttpBackend[ZIO[Any, Throwable, ?], Nothing] =
        AsyncHttpClientZioBackend()

      val clock: Clock.Service[Any] = base.clock
      val scheduler: Scheduler.Service[Any] = base.scheduler

      val userClient: UserClient.Service =
        new UserClientSTTP(cfg.userService)

      val log: Logger.Effect = new ConsoleLogger()

      def UUIDEffect: UUID.Effect = ZUUID

      def MessagePub: MessagePublisher.Effect =
        new MessagePublisherSNS(cfg.events.userMessageEvent)
    }
  }

}
