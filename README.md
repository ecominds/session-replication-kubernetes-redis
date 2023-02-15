# Session Replication on Kubernetes Using Redis
**Scenaio:** We have a microservice which uses Tomcat HTTP sessions, and we would like to scale it horizontally. We can create multiple microservice instances(pods), and use a load balancer to access them. However, each microservice instance will not have the same session data and the session won’t be consistent through microservices when the requests reach different instances. 
*One option is to use sticky sessions but even if we use sticky sessions, we will lose some session data when a microservice instance is crashed/shut down.* 

To prevent any data loss and provide consistency, session data should be replicated through microservice instances.

Multiple solutions exist for different environments and setups but here, we will find out how we can replicate sessions through Spring Boot microservices using `Redis` with only minimal configuration settings.
