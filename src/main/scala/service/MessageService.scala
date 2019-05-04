package service

import domain.{Message, User}
import scalaz.zio.ZIO

object Message {

  trait Service[R] {

    def publishMessage(message: Message): ZIO[R, Any, Any]

  }

}

object MessageService extends Message.Service[Any] {

  def publishMessage(message: Message): ZIO[Any, Any, Any] = ???

}
