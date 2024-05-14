FROM maven:3.9.4-amazoncorretto-21-al2023@sha256:92c7c50384457c0d22ee0e04d4a0a972724faa694af20ebab0cd7813c1f5cd29 AS buildtime

WORKDIR /build
COPY . .

RUN mvn clean package -DskipTests
FROM amazoncorretto:21-alpine3.19@sha256:1b755aa6e737c875a76341ca2821713d69e017658a0b0b7fb3e5e0f31ef58dd6 AS runtime
VOLUME /tmp
WORKDIR /app

COPY --from=buildtime /build/target/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]