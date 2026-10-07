FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/auth-server-*.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
