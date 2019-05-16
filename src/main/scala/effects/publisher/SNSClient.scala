package effects.publisher

import java.net.URI

import config.SnsConfig
import scalaz.zio.{Task, ZIO}
import software.amazon.awssdk.auth.credentials.{
  AwsBasicCredentials,
  StaticCredentialsProvider
}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsAsyncClient

object SNSClient {

  def instantiate(cfg: SnsConfig): Task[SnsAsyncClient] = {

    ZIO
      .succeedLazy(
        SnsAsyncClient
          .builder()
          .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(cfg.accessKey, cfg.secretKey)))
          .endpointOverride(URI.create(s"http://${cfg.host}:${cfg.port}"))
          .region(Region.of(cfg.region))
          .build())

  }

}
