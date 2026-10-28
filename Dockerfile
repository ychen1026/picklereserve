FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
RUN groupadd --system picklereserve \
    && useradd --system --gid picklereserve --home-dir /app picklereserve
WORKDIR /app
COPY --from=build /build/target/picklereserve-0.1.0.jar app.jar
RUN mkdir -p /app/data && chown -R picklereserve:picklereserve /app
USER picklereserve
EXPOSE 8080
ENV PICKLERESERVE_DB_URL=jdbc:sqlite:/app/data/picklereserve.db
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
