FROM openjdk:8u141-jdk-slim
MAINTAINER jeremydeane.net
EXPOSE 9999
RUN mkdir /app/
COPY target/docker-client-1.0.0.jar /app/
ENTRYPOINT exec java $JAVA_OPTS -Dactivemq.hostname='magic-broker' -jar /app/docker-client-1.0.0.jar