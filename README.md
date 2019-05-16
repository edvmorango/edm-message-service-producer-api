# edm-message-service-producer-api

## More infos at:
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
