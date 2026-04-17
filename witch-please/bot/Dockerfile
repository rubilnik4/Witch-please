FROM sbtscala/scala-sbt:eclipse-temurin-17.0.14_7_1.10.11_2.13.16 AS builder

WORKDIR /workspace

COPY build.sbt .
COPY project/ ./project/
COPY shared/ ./shared/
COPY bot/ ./bot/

RUN sbt --batch bot/stage

FROM eclipse-temurin:17-jre

ARG UID=10001
ARG GID=10001

WORKDIR /app

COPY --from=builder /workspace/bot/target/universal/stage/ ./

RUN groupadd --system --gid ${GID} appgroup \
  && useradd --system --uid ${UID} --gid appgroup --home-dir /app --shell /usr/sbin/nologin appuser \
  && chown -R ${UID}:${GID} /app

USER ${UID}:${GID}

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8081

ENTRYPOINT ["bin/witch-bot"]
