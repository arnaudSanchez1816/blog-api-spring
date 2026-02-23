# syntax = docker/dockerfile:1.2
# Perform the extraction in a separate builder container
FROM eclipse-temurin:25 AS builder

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

#CMD ["./mvnw", "clean", "install", "-P", "prod"]
RUN ./mvnw package -DskipTests -P prod

# https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html
# Perform the extraction in a separate builder container
FROM bellsoft/liberica-openjre-debian:25-cds AS extracter

WORKDIR /builder
# This points to the built jar file in the target folder
# Adjust this to 'build/libs/*.jar' if you're using Gradle
ARG JAR_FILE=target/*.jar
# Copy the jar file to the working directory and rename it to application.jar
COPY --from=builder ${JAR_FILE} application.jar
# Extract the jar file using an efficient layout
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# This is the runtime container
FROM bellsoft/liberica-openjre-debian:25-cds

ARG POSTGRES_URL
ENV POSTGRES_URL=$POSTGRES_URL
ARG CORS_ORIGIN_URL
ENV CORS_ORIGIN_URL=$CORS_ORIGIN_URL

RUN groupadd spring && useradd -g spring -s /bin/sh spring --create-home
# Add user to group 1000 to access secrets
RUN usermod -a -G 1000 spring
USER spring:spring

WORKDIR /application
# Copy the extracted jar contents from the builder container into the working directory in the runtime container
# Every copy step creates a new docker layer
# This allows docker to only pull the changes it really needs
COPY --from=extracter /builder/extracted/dependencies/ ./
COPY --from=extracter /builder/extracted/spring-boot-loader/ ./
COPY --from=extracter /builder/extracted/snapshot-dependencies/ ./
COPY --from=extracter /builder/extracted/application/ ./
## Execute the AOT cache training run
#RUN java -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -jar application.jar
## Start the application jar with AOT cache enabled - this is not the uber jar used by the builder
## This jar only contains application code and references to the extracted jar files
## This layout is efficient to start up and AOT cache friendly
#ENTRYPOINT ["java", "-XX:AOTCache=app.aot", "-jar", "application.jar"]
ENTRYPOINT ["java", "-jar", "application.jar"]