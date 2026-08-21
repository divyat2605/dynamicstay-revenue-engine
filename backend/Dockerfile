FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S dynamicstay && adduser -S -G dynamicstay dynamicstay
COPY --from=build /workspace/target/*.jar /app/app.jar
USER dynamicstay
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -q -O - http://127.0.0.1:8080/actuator/health/readiness | grep -q '"status":"UP"'
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
