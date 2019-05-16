package failures

sealed trait MessageError extends Exception

case class UserCannotSendMessageToHimself(email: String) extends MessageError
case class CannotFindUser(email: String) extends MessageError
case class CannotPublishMessage() extends MessageError
