package domain

import java.time.LocalDateTime

case class SendMessage(message: String,
                       sender: User,
                       peer: User,
                       sendData: LocalDateTime)
