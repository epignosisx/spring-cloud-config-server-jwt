FROM maven:3.6.3-jdk-8 AS build

COPY src /app/src
COPY pom.xml /app
RUN mvn -f /app/pom.xml clean package

FROM openjdk:8
COPY --from=build /app/target/spring-cloud-config-server-jwt-1.0.0-SNAPSHOT.jar /opt
VOLUME /config
WORKDIR /
EXPOSE 80
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar",\
            "/opt/spring-cloud-config-server-jwt-1.0.0-SNAPSHOT.jar",\
            "--server.port=80",\
            "--spring.config.name=application"]
