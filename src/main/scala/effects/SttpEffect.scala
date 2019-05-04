package effects

import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.zio.AsyncHttpClientZioBackend
import scalaz.zio.ZIO

trait Sttp {

  val SttpEffect: Sttp.Effect

}

object Sttp {

  trait Effect {

    implicit def sttpBackend: SttpBackend[ZIO[Any, Throwable, ?], Nothing]

  }

}

object ZSttp extends Sttp.Effect {

  implicit def sttpBackend: SttpBackend[ZIO[Any, Throwable, ?], Nothing] =
    AsyncHttpClientZioBackend()

}
