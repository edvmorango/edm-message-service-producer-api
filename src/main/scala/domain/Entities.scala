package domain

import java.time.{LocalDate, LocalDateTime}

case class User(uuid: String, name: String, email: String, birthDate: LocalDate)

case class Message(uuid: String,
                   message: String,
                   sender: User,
                   peer: User,
                   sendDate: LocalDateTime)
