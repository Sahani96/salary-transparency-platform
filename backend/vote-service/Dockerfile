FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=5s \
  CMD wget -qO- http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
