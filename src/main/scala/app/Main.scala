package app

import cats.effect.ExitCode
import config.ConfigLoader
import endpoint.HealthEndpoint
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import scalaz.zio.clock.Clock
import scalaz.zio.console._
import scalaz.zio.interop.catz._
import scalaz.zio.scheduler.Scheduler
import scalaz.zio.{App, TaskR, ZIO}

object Main extends App {

  type AppEnvironment = Clock

  type AppTask[A] = TaskR[AppEnvironment, A]

  def createRoutes(basePath: String) = {
    import org.http4s.implicits._

    println(basePath)
    val healthEndpoints =
      new HealthEndpoint[AppEnvironment]("health").endpoints

    val endpoints = healthEndpoints

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
          new Clock {
            val clock: Clock.Service[Any] = base.clock
            val scheduler: Scheduler.Service[Any] = base.scheduler
          }
        }
    } yield server

    program.foldM(e => putStrLn(e.getMessage) *> ZIO.succeed(1),
                  _ => ZIO.succeed(0))

  }
}
