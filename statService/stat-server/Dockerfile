FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY target/stat-server-0.0.1-SNAPSHOT.jar app.jar
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

EXPOSE 9090

ENTRYPOINT ["/app/wait-for-it.sh", "stat_postgres:5432", "--", "java", "-jar", "app.jar"]
