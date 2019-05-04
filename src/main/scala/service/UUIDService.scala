package service

import io.chrisdavenport.fuuid.FUUID
import scalaz.zio.{Task, TaskR}
import scalaz.zio.interop.catz._

trait UUIDService {

  def genUUID(): TaskR[Any, String]

}

object UUIDService extends UUIDService {

  def genUUID(): TaskR[Any, String] = FUUID.randomFUUID[Task].map(_.toString())

}
