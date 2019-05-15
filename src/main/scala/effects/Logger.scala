package effects

import scalaz.zio.{UIO, ZIO}

trait Logger {

  val log: Logger.Effect

}

object Logger {

  trait Effect {

    def error(message: String): UIO[Unit]

    def info(message: String): UIO[Unit]

  }

}

class ConsoleLogger extends Logger.Effect {

  def error(message: String): UIO[Unit] =
    ZIO.effectTotal(println(s"ERROR: $message"))

  def info(message: String): UIO[Unit] =
    ZIO.effectTotal(println(s"INFO: $message"))
}
