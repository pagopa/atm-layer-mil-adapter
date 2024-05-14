FROM maven:3.9.4-amazoncorretto-21-al2023@sha256:8af2c2a3429d0c30441f5df9da107cbf5dbdbe988bb9d6dc1e3500e5a6d77ee8 AS buildtime

WORKDIR /build
COPY . .

RUN mvn clean package -DskipTests
FROM amazoncorretto:21-alpine3.19@sha256:7e522a694566c0c6cd80b06d97bc69f4be31a518d81d6cdd30c9a854a56aa84a AS runtime
VOLUME /tmp
WORKDIR /app

COPY --from=buildtime /build/target/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]