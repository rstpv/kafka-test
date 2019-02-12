FROM openjdk:8-jdk-alpine as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN target=/root/.m2 ./mvnw verify clean --fail-never

COPY src src
RUN target=/root/.m2 ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /opt/app/lib
COPY --from=build ${DEPENDENCY}/META-INF /opt/app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /opt/app

RUN addgroup -S app && adduser -S -G app app && \
    chown app -R /opt/

USER app
CMD ["java","-cp","/opt/app:/opt/app/lib/*","co.ceiba.example.kafka.KafkaApplication"]