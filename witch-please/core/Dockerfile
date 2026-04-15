FROM sbtscala/scala-sbt:eclipse-temurin-17.0.14_7_1.10.11_2.13.16 AS builder

WORKDIR /workspace

COPY build.sbt .
COPY project/ ./project/
COPY shared/ ./shared/
COPY core/ ./core/

RUN sbt --batch core/stage

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/core/target/universal/stage/ ./

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

ENTRYPOINT ["bin/witch-core"]
