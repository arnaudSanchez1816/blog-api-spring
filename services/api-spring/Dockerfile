# syntax = docker/dockerfile:1.2

#FROM eclipse-temurin:25
##RUN addgroup -s spring && adduser -s spring -G spring
#RUN groupadd spring && useradd -g spring -s /bin/sh spring
#RUN usermod -a -G 1000 spring
#USER spring:spring
#ARG DEPENDENCY=target/dependency
#COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
#COPY ${DEPENDENCY}/META-INF /app/META-INF
#COPY ${DEPENDENCY}/BOOT-INF/classes /app
#ENTRYPOINT ["java","-cp","app:app/lib/*","com.blog.api.apispring.ApiSpringApplication"]
FROM eclipse-temurin:25
RUN groupadd spring && useradd -g spring -s /bin/sh spring --create-home
RUN usermod -a -G 1000 spring
USER spring:spring
RUN --mount=type=secret,id=_env,dst=/etc/secrets/.env
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY docker-compose.yml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

#CMD ["./mvnw", "clean", "install", "-P", "prod"]
CMD ["./mvnw", "spring-boot:run", "-P", "prod"]