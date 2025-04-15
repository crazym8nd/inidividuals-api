FROM bellsoft/liberica-runtime-container:jdk-21.0.4-slim-stream-glibc as builder

WORKDIR /build
COPY ./build/libs/individuals-api-0.0.1-SNAPSHOT.jar ./individuals-api.jar

FROM bellsoft/liberica-runtime-container:jdk-21.0.4-slim-stream-glibc

LABEL maintainer="crazym8nd"
LABEL version="0.0.1"
LABEL description="Individuals API service"

WORKDIR /app
COPY --from=builder /build/individuals-api.jar ./individuals-api.jar

RUN chown -R nobody:nobody /app
USER nobody

# Предполагаем, что приложение работает на порту 8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

CMD [ "sh", "-c", "java $JAVA_OPTS -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -jar /app/individuals-api.jar" ]
