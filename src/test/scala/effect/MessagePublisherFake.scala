package effect

import domain.MessageSent
import effects.publisher.MessagePublisher
import scalaz.zio.ZIO

class MessagePublisherFake() extends MessagePublisher.Effect {

  def publishMessage(message: MessageSent): ZIO[Any, Throwable, Unit] = ZIO.unit

}
