package service

import domain.User
import scalaz.zio.ZIO

trait UserService[R] {

  def findByEmail(email: String): ZIO[R, Throwable, Option[User]]

}
class UserServiceImpl extends UserService[Any] {

  def findByEmail(email: String): ZIO[Any, Throwable, Option[User]] = ???

}
