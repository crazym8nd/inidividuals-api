FROM bellsoft/liberica-runtime-container:jdk-21.0.4-slim-stream-glibc

WORKDIR /app
COPY ./build/libs/individuals-api-0.0.1-SNAPSHOT.jar /app/individuals-api.jar

RUN chown nobody /app
USER nobody

CMD [ "sh", "-c", "java $JAVA_OPTS -jar /app/individuals-api.jar" ]
