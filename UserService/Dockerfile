FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

COPY JavaLabCore/pom.xml JavaLabCore/pom.xml
COPY JavaLabCore/src     JavaLabCore/src
RUN mvn -f JavaLabCore/pom.xml install -DskipTests -q

COPY UserService/pom.xml UserService/pom.xml
RUN mvn -f UserService/pom.xml dependency:go-offline -q

COPY UserService/src UserService/src
RUN mvn -f UserService/pom.xml package -DskipTests -q

WORKDIR /build/UserService
RUN java -Djarmode=tools \
         -jar target/UserService-0.0.1-SNAPSHOT.jar \
         extract --layers --launcher \
         --destination extracted

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --chown=appuser:appgroup --from=builder /build/UserService/extracted/dependencies/          ./
COPY --chown=appuser:appgroup --from=builder /build/UserService/extracted/spring-boot-loader/    ./
COPY --chown=appuser:appgroup --from=builder /build/UserService/extracted/snapshot-dependencies/ ./
COPY --chown=appuser:appgroup --from=builder /build/UserService/extracted/application/           ./

USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]