# syntax=docker/dockerfile:1.7

ARG MAVEN_VERSION=3.9.9
ARG JAVA_VERSION=17

FROM --platform=$BUILDPLATFORM maven:${MAVEN_VERSION}-eclipse-temurin-${JAVA_VERSION} AS deps
WORKDIR /app
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
RUN ./mvnw -B -DskipTests dependency:go-offline

FROM --platform=$BUILDPLATFORM maven:${MAVEN_VERSION}-eclipse-temurin-${JAVA_VERSION} AS builder
WORKDIR /app
COPY --from=deps /root/.m2 /root/.m2
COPY . .
RUN ./mvnw -B -DskipTests clean package

FROM --platform=$TARGETPLATFORM eclipse-temurin:${JAVA_VERSION}-jre-jammy AS runner
WORKDIR /app
ENV JAVA_OPTS=""

COPY --from=builder /app/target /tmp/target
RUN cp "$(find /tmp/target -maxdepth 1 -name '*.jar' ! -name '*.original' | head -n 1)" /app/app.jar \
    && rm -rf /tmp/target

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
