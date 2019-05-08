package service

import effects.{Logger, UUID}
import effects.external.UserClient
import effects.publisher.MessagePublisher

object Environment {

  type MessageServiceEnvironment = Logger
    with UUID
    with UserClient
    with MessagePublisher

}
