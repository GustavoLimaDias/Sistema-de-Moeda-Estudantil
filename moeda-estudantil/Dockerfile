# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# ---------- runtime ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/moeda-estudantil-*.jar app.jar
# Plano free do Render: 512 MB de RAM
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080
# Render entrega postgresql://user:pass@host[:port]/db; o driver JDBC exige jdbc:postgresql://host[:port]/db
ENTRYPOINT ["sh", "-c", "if [ -z \"$DB_URL\" ] && [ -n \"$DATABASE_URL\" ]; then export DB_URL=\"jdbc:postgresql://${DATABASE_URL#*@}\"; fi; exec java $JAVA_OPTS -Dspring.profiles.active=prod -jar app.jar"]
