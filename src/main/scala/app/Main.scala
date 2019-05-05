package app

import cats.effect.ExitCode
import cats.syntax.all._
import config.ConfigLoader
import effects._
import endpoint.{HealthEndpoint, MessageEndpoint}
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import scalaz.zio.clock.Clock
import scalaz.zio.console._
import scalaz.zio.interop.catz._
import scalaz.zio.scheduler.Scheduler
import scalaz.zio.{App, TaskR, ZIO}

object Main extends App {

  type AppEnvironment = Clock with UUID with UserClient

  type AppTask[A] = TaskR[AppEnvironment, A]

  def createRoutes(basePath: String) = {
    import org.http4s.implicits._

    val healthEndpoints =
      new HealthEndpoint[AppEnvironment]("health").endpoints

    val messageEndpoints =
      new MessageEndpoint[AppEnvironment]("message").endpoints

    val endpoints = healthEndpoints <+> messageEndpoints

    Router[AppTask](basePath -> endpoints).orNotFound

  }

  def run(args: List[String]): ZIO[Environment, Nothing, Int] = {

    val program = for {
      cfg <- ZIO.fromEither(ConfigLoader.load)
      httpApp = createRoutes(cfg.app.context)
      server <- ZIO
        .runtime[AppEnvironment]
        .flatMap { implicit rts =>
          BlazeServerBuilder[AppTask]
            .bindHttp(cfg.app.port, "0.0.0.0")
            .withHttpApp(httpApp)
            .serve
            .compile[AppTask, AppTask, ExitCode]
            .drain
        }
        .provideSome[Environment] { base =>
          new Clock with UUID with UserClient {

            val clock: Clock.Service[Any] = base.clock
            val scheduler: Scheduler.Service[Any] = base.scheduler

            val userClient: UserClient.Service =
              new UserClientSTTP(cfg.userService)

            def UUIDEffect: UUID.Effect = ZUUID

          }
        }
    } yield server

    program.foldM(e => putStrLn(e.getMessage) *> ZIO.succeed(1),
                  _ => ZIO.succeed(0))

  }
}
