package effects.publisher

import akka.stream.alpakka.sns.scaladsl.SnsPublisher
import akka.stream.scaladsl.{Sink, Source}
import domain.MessageSent
import scalaz.zio.ZIO
import software.amazon.awssdk.services.sns.SnsAsyncClient

trait MessagePublisher {

  def MessagePub: MessagePublisher.Effect

}

object MessagePublisher {

  trait Effect {

    def publishMessage(message: MessageSent): ZIO[Any, Throwable, Unit]

  }

}

class MessagePublisherSNS(topic: String, implicit val snsClient: SnsAsyncClient)
    extends MessagePublisher.Effect {

  import io.circe.generic.auto._
  import io.circe.syntax._

  def publishMessage(message: MessageSent): ZIO[Any, Throwable, Unit] = {

    for {
      _ <- ZIO
        .fromFuture { implicit ec =>
          Source
            .single(message.asJson.toString())
            .via(SnsPublisher.flow(topic))
            .runWith(Sink.ignore)
        }
    } yield ()

  }

}
