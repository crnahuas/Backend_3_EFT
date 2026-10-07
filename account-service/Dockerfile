FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/account-service-*.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
