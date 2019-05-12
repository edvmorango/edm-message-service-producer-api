package service

import java.time.LocalDate

import domain.{SendMessage, User}
import effect.{MessagePublisherFake, UserClientInMemory}
import effects.external.UserClient
import effects.publisher.MessagePublisher
import effects.{ConsoleLogger, Logger, UUID, ZUUID}
import environment.Environments.MessageServiceEnvironment
import org.scalatest.{MustMatchers, WordSpec}
import scalaz.zio.{DefaultRuntime, ZIO}
class MessageServiceSpec
    extends WordSpec
    with MustMatchers
    with DefaultRuntime {

  val users = Set(
    User("uuid1",
         "Eduardo Morango",
         "jevmorr@gmail.com",
         LocalDate.of(1996, 6, 10)),
    User("uuid2", "José  Vieira", "jevmor@gmail.com", LocalDate.of(1996, 6, 10))
  )

  val specEnvironment: MessageServiceEnvironment =
    new Logger with UUID with UserClient with MessagePublisher {
      val log: Logger.Effect = new ConsoleLogger()

      def UUIDEffect: UUID.Effect = ZUUID

      val userClient: UserClient.Service = new UserClientInMemory(users)

      def MessagePub: MessagePublisher.Effect = new MessagePublisherFake()
    }

  def unsafeRunProviding[A](f: ZIO[MessageServiceEnvironment, Throwable, A]) =
    unsafeRun {
      f.provide(specEnvironment)
    }

  import MessageServiceImpl._

  "MessageService" should {

    val validMessage =
      SendMessage("hey there", "jevmorr@gmail.com", "jevmor@gmail.com")

    val invalidMessage = validMessage.copy(peerEmail = "jevmorr@gmail.com")

    "Publish a valid message" in {

      unsafeRunProviding(publishMessage(validMessage)).message mustBe validMessage.message

    }

    "Not publish a message when a user send a message for himself" in {

      unsafeRunProviding(publishMessage(invalidMessage).either).isLeft mustBe true

    }

  }

}
