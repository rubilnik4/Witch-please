FROM sbtscala/scala-sbt:eclipse-temurin-17.0.14_7_1.10.11_2.13.16

WORKDIR /app

COPY build.sbt .
COPY project/ ./project/

RUN sbt update

COPY . .

EXPOSE 8080

CMD ["sbt", "run"]