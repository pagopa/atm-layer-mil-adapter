FROM maven:3.11.0-amazoncorretto-21-al2023@sha256:c7719f952f62e301c6c24b86ef9a2ea1cd0a314a862ed12e51f0ffbc3fbb96b5 AS buildtime

WORKDIR /build
COPY . .

RUN mvn clean package -DskipTests
FROM amazoncorretto:21-alpine3.19@sha256:34650d7c653af234dad21cd2d89d2f0dbdb1bad54041014932e51b3492e0dec5 AS runtime
VOLUME /tmp
WORKDIR /app

COPY --from=buildtime /build/target/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]