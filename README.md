# Sexy Image eco-system
1. Intellij Plugin: [live-sexyeditor](https://github.com/conanchen/live-sexyeditor)
2. Android App: [hiask-android-sexyimage](https://github.com/conanchen/hiask-android-sexyimage)
3. Grpc Api: [hiask-api-sexyimage](https://github.com/conanchen/hiask-api-sexyimage)
4. Cloud Service: [hiask-cloud-sexyimage](https://github.com/conanchen/hiask-cloud-sexyimage)


# Sexy Images  Cloud Service
- springboot
- grpc
- ignite

Follow https://www.javacodegeeks.com/2017/07/apache-ignite-spring-data.html 

to integrate Spring Boot. 

# application.properties
- gRPC Server started, listening on port 8980.
grpc.port=8980 
- Tomcat initialized with port(s): 8088 (http)
server.port=8088
- ignite-rest-http http port: Started ServerConnector@23565287{HTTP/1.1}{0.0.0.0:8080}
  default, refer to https://apacheignite.readme.io/v1.0/docs/rest-api
  - $ cd /Users/admin/ignite-web-agent-2.1.4
  - $ ./ignite-web-agent.sh &
  - open browser : https://console.gridgain.com