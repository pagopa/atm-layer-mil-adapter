FROM maven:3.9.4-amazoncorretto-21-al2023 AS buildtime

WORKDIR /build
COPY . .

RUN mvn clean package -DskipTests
FROM amazoncorretto:21-alpine3.19 AS runtime
VOLUME /tmp
WORKDIR /app

COPY --from=buildtime /build/target/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]