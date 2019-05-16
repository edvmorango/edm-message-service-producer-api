# edm-message-service-producer-api


## Changelog

###__05/16 - Typed failures__

__Failures__: At __service__ and __main__ (happens at Domain and DI).

__Defects__: Occurs mainly at __effects__ (happens in Total Effects and external interactions).


   

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
