package config

final case class Config(app: AppConfig,
                        aws: AWSConfig,
                        events: EventConfig,
                        userConfig: UserConfig)

final case class AppConfig(host: String, context: String, port: Int)

final case class AWSConfig(sns: SnsConfig)

final case class SnsConfig(prefix: String,
                           region: String,
                           host: String,
                           port: Int,
                           accessKey: String,
                           secretKey: String)

final case class EventConfig(userMessageEvent: String)

final case class UserConfig(baseUri: String)

object ConfigLoader {
  import java.nio.file.Path
  import pureconfig.generic.auto._
  import scala.util.Try
  import pureconfig.module.yaml._

  def load: Either[Throwable, Config] = {

    val path = Path of ClassLoader.getSystemResource("application.yaml").getPath

    Try(loadYamlOrThrow[Config](path)).toEither

  }

}
