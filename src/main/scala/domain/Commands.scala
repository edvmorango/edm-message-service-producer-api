package domain

final case class SendMessage(message: String,
                             senderEmail: String,
                             peerEmail: String)
