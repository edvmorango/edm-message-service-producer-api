# edm-message-service-producer-api


## Changelog

###__19/05/16 - Typed failures__

__Failures__: Must occur during the dependency injection and inside the domain.

Examples at __service__ and __main__.

__Defects__: Used to wrap total effects or external errors (JVM errors and external services).

Examples at __effects__ using __UIO__

__Obs__: The __defects__ approach is subject to changes.

## More info
https://github.com/edvmorango/event-driven-messenger

## Stack 
__(ZIO + Http4s + sttp + sns)__



## Example request (will be replaced by __*OpenAPI*__ + __*AsyncApi*__ soon)

```
POST /message/v1/message 
Host: localhost:8081
{
    "message": "ZIO is awesome!",
    "senderEmail": "sender@gmail.com",
    "peerEmail": "peer@gmail.com"
}
```
