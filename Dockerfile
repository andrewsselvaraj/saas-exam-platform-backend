# One image per service. Build the jars first (./mvnw -DskipTests package), then e.g.
#   docker build --build-arg SERVICE=catalog-service -t saasexam/catalog-service .
# docker-compose.yml builds all four.
FROM eclipse-temurin:17-jre
ARG SERVICE
WORKDIR /app
COPY ${SERVICE}/target/${SERVICE}-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
