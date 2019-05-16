package effect

import domain.User
import effects.external.UserClient
import scalaz.zio.{UIO, ZIO}

class UserClientInMemory(users: Set[User]) extends UserClient.Service {

  def findByEmail(email: String): UIO[Option[User]] =
    ZIO.succeedLazy(users.find(_.email == email))

}
