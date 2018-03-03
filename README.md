# Akka Microservice App 



### Dependencies 

Run zipkin in docker:
```
docker run --rm -it -e "QUERY_LOG_LEVEL=DEBUG" -e "HOSTNAME=xxx.server.com" -e "logging.level.com.facebook.swift.service.ThriftServiceProcessor=INFO" -p 9411:9411 openzipkin/zipkin
```


### Build 

Turns out it's pretty easy to build a container ready to run. Just do the following to publish to your local docker repo: 
```sh
sbt docker:publishLocal
```
