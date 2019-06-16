# Spring Cloud Config Server with JWT security
A docker image of [Spring Cloud Config Server](https://cloud.spring.io/spring-cloud-static/spring-cloud-config/2.1.2.RELEASE/single/spring-cloud-config.html) with JWT security.

The JWT support is as follows: 

- Update the application.yml to have a `jwt.secret=the-signing-key` with the JWT signing key or use environment variable `JWT_SECRET=the-signing-key`.
- The JWT must have a scope property with an array of [ant path patterns](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/AntPathMatcher.html) of allowed urls:
  ```json
  {
    "sub": "app1",
    "scope": ["/shared,app1/dev/**", "/shared,app1/qa/**"]
  }
  ```
  This is flexible enough to allow one app to have access to multiple configurations and to have different tokens per profile (dev vs prod)

- The signing key algorithm is HS512.

- Sample request
  ```shell
  curl --header "Authorization: Bearer <Enter JWT>" http://localhost:8080/foo/development
  ``` 

- You can use a website like https://jwt.io/ to generate tokens.

If you are interested in accepting HTTPS traffic, [this is a good guide](https://drissamri.be/blog/java/enable-https-in-spring-boot/) on how to enable it.

## Usage
```
docker run -it --name=spring-cloud-config-server \
      -p 8888:8888 \
      -v </path/to/config>:/config \
      hyness/spring-cloud-config-server
```

#### Parameters
* `-p 8888` Server port
* `-v /config` Mounted configuration

###  Configuring Spring Cloud Config Server
Spring Cloud Config Server is a normal Spring Boot application, it can be configured through all the ways a Spring Boot application can be configured.  You may use environment variables or you can mount configuration in the provided volume.  The configuration file must be named **application** and may be a properties or yaml file. See the [Spring Boot documentation](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-external-config) for further information on how to use and configure Spring Boot.


#### Configuration examples
```
# Using a mounted config Directory
docker run -it -p 8888:8888 \
      -v /path/to/config/dir:/config \
      hyness/spring-cloud-config-server

# Using a mounted application.yml
docker run -it -p 8888:8888 \
      -v /path/to/application.yml:/config/application.yml \
      hyness/spring-cloud-config-server

# Configure through environment variables without a configuration file
docker run -it -p 8888:8888 \
      -e SPRING_CLOUD_CONFIG_SERVER_GIT_URI=https://github.com/spring-cloud-samples/config-repo \
      -e JWT_SECRET=your-signing-key \
      hyness/spring-cloud-config-server

# Configure through command line arguments without a configuration file
docker run -it -p 8888:8888 \
      hyness/spring-cloud-config-server \
      --spring.cloud.config.server.git.uri=https://github.com/spring-cloud-samples/config-repo \
      --jwt.secret=your-signing-key
```
#### Verify Samples Above
```
$ curl --header "Authorization Bearer <Enter JWT>" http://localhost:8888/foo/development
```

### Required Backend Configuration
Spring Cloud Config Server **requires** that you configure a backend to serve your configuration files.  There are currently 3 backends to choose from...

#### Git
```
# Github example
docker run -it -p 8888:8888 \
      -e SPRING_CLOUD_CONFIG_SERVER_GIT_URI=https://github.com/spring-cloud-samples/config-repo \
      hyness/spring-cloud-config-server

# Local git repo example
docker run -it -p 8888:8888 \
      -v /path/to/config/files/dir:/config \
      -e SPRING_CLOUD_CONFIG_SERVER_GIT_URI=file:/config/my-local-git-repo \
      hyness/spring-cloud-config-server
```

#### Filesystem
```
docker run -it -p 8888:8888 \
      -v /path/to/config/files/dir:/config \
      -e SPRING_PROFILES_ACTIVE=native \
      hyness/spring-cloud-config-server
```

#### Vault
```
docker run -it -p 8888:8888 \
      -e SPRING_PROFILES_ACTIVE=vault \
      hyness/spring-cloud-config-server
```