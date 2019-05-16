package app

import cats.effect.ExitCode
import cats.syntax.all._
import config.ConfigLoader
import effects.publisher.SNSClient
import endpoint.{HealthEndpoint, MessageEndpoint}
import environment.Environments
import environment.Environments.{AppEnvironment, AppTask}
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import scalaz.zio.console._
import scalaz.zio.interop.catz._
import scalaz.zio.{App, ZIO}

object Main extends App {

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
      sns <- SNSClient.instantiate(cfg.aws.sns)
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
        .provideSome[Environment](Environments.createEnvironment(cfg, sns))

    } yield server

    program.foldM(e => putStrLn(e.getMessage) *> ZIO.succeed(1),
                  _ => ZIO.succeed(0))

  }
}
