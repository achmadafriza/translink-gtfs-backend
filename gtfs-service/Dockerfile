# the first stage of our build will use a gradle 7.5 parent image
FROM gradle:7.5.1-jdk8 AS GRADLE_BUILD

# copy the pom and src code to the container
COPY ./ /home
WORKDIR /home

USER root
RUN chown -R gradle /home

# package our application code
RUN gradle clean assemble

# the second stage of our build will use open jdk 11 on alpine
FROM openjdk:8-jdk-alpine

USER root
# copy only the artifacts we need from the first stage and discard the rest
COPY --from=GRADLE_BUILD /home/build/libs/*.jar /home/app.jar
RUN apk --no-cache add curl

ARG server_port=8080
ENV PORT=$server_port

# set the startup command to execute the jar
ENTRYPOINT ["java", "-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector", "-jar", "/home/app.jar"]
HEALTHCHECK --interval=20s --timeout=3s --retries=20 \
    CMD curl --fail http://localhost:8080/actuator/health || exit 1
