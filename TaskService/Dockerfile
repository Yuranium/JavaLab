FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

COPY JavaLabCore/pom.xml JavaLabCore/pom.xml
COPY JavaLabCore/src     JavaLabCore/src
RUN mvn -f JavaLabCore/pom.xml install -DskipTests -q

COPY TaskService/pom.xml TaskService/pom.xml
RUN mvn -f TaskService/pom.xml dependency:go-offline -Djooq.codegen.skip=true -q

COPY TaskService/target/generated-sources/jooq TaskService/target/generated-sources/jooq

COPY TaskService/src TaskService/src
RUN mvn -f TaskService/pom.xml package -DskipTests -Djooq.codegen.skip=true -q

WORKDIR /build/TaskService
RUN java -Djarmode=tools \
         -jar target/TaskService-0.0.1-SNAPSHOT.jar \
         extract --layers --launcher \
         --destination extracted \

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --chown=appuser:appgroup --from=builder /build/TaskService/extracted/dependencies/          ./
COPY --chown=appuser:appgroup --from=builder /build/TaskService/extracted/spring-boot-loader/    ./
COPY --chown=appuser:appgroup --from=builder /build/TaskService/extracted/snapshot-dependencies/ ./
COPY --chown=appuser:appgroup --from=builder /build/TaskService/extracted/application/           ./

USER appuser
EXPOSE 8085
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]