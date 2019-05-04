package domain

import java.time.{LocalDate, LocalDateTime}

final case class User(uuid: String,
                      name: String,
                      email: String,
                      birthDate: LocalDate)

final case class Message(uuid: String,
                         message: String,
                         sender: User,
                         peer: User,
                         sendDate: LocalDateTime)
