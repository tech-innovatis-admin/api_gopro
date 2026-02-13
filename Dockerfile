# syntax=docker/dockerfile:1.7

ARG MAVEN_VERSION=3.9.9
ARG JAVA_VERSION=17

FROM --platform=$BUILDPLATFORM maven:${MAVEN_VERSION}-eclipse-temurin-${JAVA_VERSION} AS builder
WORKDIR /workspace
COPY --link pom.xml ./
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests dependency:go-offline
COPY --link src ./src
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests clean package

FROM --platform=$TARGETPLATFORM eclipse-temurin:${JAVA_VERSION}-jre-jammy AS runner
WORKDIR /app
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=builder /workspace/target /tmp/target
RUN set -eux; \
    JAR_PATH="$(find /tmp/target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n 1)"; \
    test -n "$JAR_PATH"; \
    cp "$JAR_PATH" /app/app.jar; \
    rm -rf /tmp/target

EXPOSE 8080
USER spring:spring
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
