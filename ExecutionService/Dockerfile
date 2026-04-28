FROM eclipse-temurin:23-jdk-alpine AS maven-base
ENV MAVEN_VERSION=3.9.9
ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

RUN apk add --no-cache curl && \
    curl -fsSL \
         "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
         -o /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    mv /opt/apache-maven-${MAVEN_VERSION} /opt/maven && \
    rm /tmp/maven.tar.gz && \
    apk del curl

FROM maven-base AS builder
WORKDIR /build

COPY JavaLabCore/pom.xml JavaLabCore/pom.xml
COPY JavaLabCore/src     JavaLabCore/src
RUN mvn -f JavaLabCore/pom.xml install -DskipTests -q

COPY ExecutionService/pom.xml ExecutionService/pom.xml
RUN mvn -f ExecutionService/pom.xml dependency:go-offline -Djooq.codegen.skip=true -q

COPY ExecutionService/target/generated-sources/jooq ExecutionService/target/generated-sources/jooq

COPY ExecutionService/src ExecutionService/src
RUN mvn -f ExecutionService/pom.xml package -DskipTests -Djooq.codegen.skip=true -q

WORKDIR /build/ExecutionService
RUN java -Djarmode=tools \
         -jar target/ExecutionService-0.0.1-SNAPSHOT.jar \
         extract --layers --launcher \
         --destination extracted

FROM eclipse-temurin:23-jdk-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --chown=appuser:appgroup --from=builder /build/ExecutionService/extracted/dependencies/          ./
COPY --chown=appuser:appgroup --from=builder /build/ExecutionService/extracted/spring-boot-loader/    ./
COPY --chown=appuser:appgroup --from=builder /build/ExecutionService/extracted/snapshot-dependencies/ ./
COPY --chown=appuser:appgroup --from=builder /build/ExecutionService/extracted/application/           ./

USER appuser
EXPOSE 8084
ENTRYPOINT ["java", \
            "--add-opens=java.base/java.lang=ALL-UNNAMED", \
            "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED", \
            "org.springframework.boot.loader.launch.JarLauncher"]