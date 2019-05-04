package domain

import java.time.LocalDateTime

final case class SendMessage(message: String,
                             sender: User,
                             peer: User,
                             sendData: LocalDateTime)
