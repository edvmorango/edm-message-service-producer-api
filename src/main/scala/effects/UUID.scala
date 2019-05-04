package effects

import io.chrisdavenport.fuuid.FUUID
import scalaz.zio.interop.catz._
import scalaz.zio.{Task, TaskR}

trait UUID {

  def UUIDEffect: UUID.Effect

}

object UUID {

  trait Effect {

    def genUUID(): TaskR[Any, String]

  }

}

object ZUUID extends UUID.Effect {

  def genUUID(): TaskR[Any, String] = FUUID.randomFUUID[Task].map(_.toString())

}
