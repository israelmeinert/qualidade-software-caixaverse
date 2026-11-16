FROM eclipse-temurin:21.0.13_9-jre-jammy@sha256:dff6eb46527bd293a3c06bbbe9e64e8c9f91f01905b5abbdad31761c03059d2c
WORKDIR /opt/rewards
COPY --chown=10001:10001 target/quarkus-app/ ./
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
