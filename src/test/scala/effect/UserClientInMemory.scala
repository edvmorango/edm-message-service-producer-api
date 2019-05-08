package effect

import domain.User
import effects.external.UserClient
import scalaz.zio.ZIO

class UserClientInMemory(users: Set[User]) extends UserClient.Service {

  def findByEmail(email: String): ZIO[Any, Throwable, Option[User]] =
    ZIO.succeedLazy(users.find(_.email == email))

}
