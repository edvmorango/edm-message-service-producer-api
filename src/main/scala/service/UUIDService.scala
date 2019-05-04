package service

import io.chrisdavenport.fuuid.FUUID
import scalaz.zio.{Task, TaskR}
import scalaz.zio.interop.catz._

trait UUID {

  def uuidService: UUID.Service

}

object UUID {

  trait Service {

    def genUUID(): TaskR[Any, String]

  }

}

object UUIDService extends UUID.Service {

  def genUUID(): TaskR[Any, String] = FUUID.randomFUUID[Task].map(_.toString())

}
