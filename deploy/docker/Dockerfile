# One image per microservice, built from source, for builds outside OpenShift
# (for example to push to an external registry). In-cluster builds use the S2I BuildConfigs instead.
#
# Build from the repo root, once per service:
#   docker build -f deploy/docker/Dockerfile \
#     --build-arg SERVICE=catalog-service -t catalog-service .
# SERVICE is one of: api-gateway, catalog-service, question-service, exam-service.

FROM registry.access.redhat.com/ubi9/openjdk-17:1.21 AS build
ARG SERVICE
WORKDIR /home/default/app
COPY --chown=185:0 . .
RUN test -n "$SERVICE" && \
    mvn -B package -DskipTests -pl "$SERVICE" -am && \
    cp "$SERVICE/target/$SERVICE-0.0.1-SNAPSHOT.jar" /tmp/app.jar

FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.21
# run-java.sh in this image starts the single jar found in /deployments and sizes
# the heap from the container memory limit.
COPY --from=build --chown=185:0 /tmp/app.jar /deployments/app.jar
USER 185
