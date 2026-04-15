FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradle/ gradle/
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY build.gradle.kts settings.gradle.kts ./
COPY src/ src/

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
