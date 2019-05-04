package service

import config.UserConfig
import domain.User
import scalaz.zio.ZIO

object User {

  trait Service[R] {

    def findByEmail(email: String): ZIO[R, Throwable, Option[User]]

  }

}

class UserService(userServiceConfig: UserConfig) extends User.Service[Any] {

  def findByEmail(email: String): ZIO[Any, Throwable, Option[User]] = ???

}
