FROM openjdk:11-jre-slim

WORKDIR /app

COPY ./AuthenticationService.jar .

EXPOSE 8080

CMD ["java", "-jar", "AuthenticationService.jar"]
