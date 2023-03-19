FROM openjdk:11 as app
RUN mkdir -p /debug-app
WORKDIR /debug-app
ARG JAR_FILE=target/debug-app*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Xmx2048M", "-jar", "app.jar"]
