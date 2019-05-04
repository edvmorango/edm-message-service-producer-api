package service

import domain.Message
import scalaz.zio.ZIO

trait MessageService[R] {

  def publishMessage(message: Message): ZIO[R, Any, Any]

}

object MessageServiceImpl extends MessageService[Any] {

  def publishMessage(message: Message): ZIO[Any, Any, Any] = ???

}
