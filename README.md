# Session Replication on Kubernetes Using Redis
**Scenaio:** We have a microservice which uses Tomcat HTTP sessions, and we would like to scale it horizontally. We can create multiple microservice instances(pods), and use a load balancer to access them. However, each microservice instance will not have the same session data and the session won’t be consistent through microservices when the requests reach different instances. 
*One option is to use sticky sessions but even if we use sticky sessions, we will lose some session data when a microservice instance is crashed/shut down.* 

To prevent any data loss and provide consistency, session data should be replicated through microservice instances.

Multiple solutions exist for different environments and setups but here, we will find out how we can replicate sessions through Spring Boot microservices using `Redis` with only minimal configuration settings.

## Develop and run a simple springboot web application

<details>
<summary><b>See Steps</b></summary>

#### Create a Dockerfile in root path of the application with the below content

```
FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
VOLUME /tmp
ENTRYPOINT ["java","-jar","/app.jar"]
```

- Run in docker environment and test
```
# Build a docker container
$ docker build -t ecominds/boot-webapp .
```
```
# Execute the below command to satrt the container
$ docker run -it -d --rm --name=boot-webapp -p 80:8080 ecominds/boot-webapp
```

</details>

### Testing
  - Access the application on browser several times to increment the hits.
  - Restart the `web-app` and access/refresh the page in browser and notice, **the count is reset to 1**
  
*That is because each java application stores its sessions separately*

## Use Redis for (Spring) Session Replication

### Pre-requisite
<details>
<summary><b>Make Sure that the Redis instance is running. See steps</b></summary>

#### Run Redis on docker container
- Execute the below command to download the latest version and start it

```
$ docker run -d -it --name=docker_redis -p 6379:6379 redis/redis-stack:latest
```

- Execute the below command to test
```
$ $ docker exec -it docker_redis redis-cli ping
```

And the output will be **PONG**

</details>

#### In order to use Spring Session with Redis, make the followiing changes and re-build the docker image
- add `redis` dependency in `pom.xml`
- add `spring-session` configuration in `application.properties`
- Include `@EnableRedisHttpSession` in `main` class

##### pom.xml changes
```
<dependency>
	<groupId>org.springframework.session</groupId>
	<artifactId>spring-session-core</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.session</groupId>
	<artifactId>spring-session-data-redis</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```
##### application.properties changes
```
spring.session.store-type=redis
spring.redis.host=localhost
spring.redis.port=6379
server.servlet.session.timeout = 5m # Session Timeout
```

## Build and deploy on Kubernetes cluster
1. Re-build the docker image
```
$ docker build -t ecominds/boot-webapp .
```

2. Create a deployment configuration `app-deploy.yml` with the below content
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: boot-webapp
  labels:
    owner: ecominds
    app: boot-webapp
spec:
  selector:
    matchLabels:
      app: boot-webapp
  replicas: 2
  template:
    metadata:
      labels:
        app: boot-webapp
    spec:
      containers:
      - name: boot-webapp
        image: ecominds/boot-webapp
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8081
        env:
        - name: server.port
          value: "8081"
        - name: spring.redis.host
          value: "192.168.65.4"
        - name: server.servlet.session.timeout
          value: "10"          
---     
apiVersion: v1
kind: Service
metadata:
  name: boot-webapp-lb
spec:
  selector:
    app: boot-webapp
  ports:
  - name: http
    port: 80
    targetPort: 8081
    protocol: TCP
  type: LoadBalancer
```

**Note:** Get the IP of the Kubernetes cluster and configure into `app-deploy.yml` as `spring.redis.host`, because, Redis is running in a separate docker container
```
$ kubectl get nodes -o wide
# Internal IP : 192.168.65.4
```

3. Execuute the below command to start the application
```
$ kubectl apply -f .\app-deploy.yml
```

4. Verify the status
```
$ kubectl get deployments,services,pods
NAME                          READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/boot-webapp   2/2     2            2           12s

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
service/boot-webapp-lb   LoadBalancer   10.109.126.100   localhost     80:30448/TCP   12s
service/kubernetes       ClusterIP      10.96.0.1        <none>        443/TCP        3h36m

NAME                               READY   STATUS    RESTARTS   AGE
pod/boot-webapp-85dc8f5bb9-krn5q   1/1     Running   0          12s
pod/boot-webapp-85dc8f5bb9-rwt5l   1/1     Running   0          12s
```

### Testing
  - Access the application on browser several times to increment the hits.
  - Restart/kill the `pods` and access/refresh the page in browser and notice, **the count will be incremented by 1**
  - Wait for 10 or more minutes and then acccess the application, **the count will reset to 1**