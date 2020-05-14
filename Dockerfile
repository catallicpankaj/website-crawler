FROM openjdk:8-jre-slim

WORKDIR /usr/src/app

COPY target/crawler-app-0.0.1.jar ./

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx512m", "-jar", "/usr/src/app/crawler-app-0.0.1.jar"]
