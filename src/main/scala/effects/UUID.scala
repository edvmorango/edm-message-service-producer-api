package effects

import io.chrisdavenport.fuuid.FUUID
import scalaz.zio.interop.catz._
import scalaz.zio.{Task, UIO}

trait UUID {

  def UUIDEffect: UUID.Effect

}

object UUID {

  trait Effect {

    def genUUID(): UIO[String]

  }

}

object ZUUID extends UUID.Effect {

  def genUUID(): UIO[String] = FUUID.randomFUUID[Task].map(_.toString()).orDie

}
