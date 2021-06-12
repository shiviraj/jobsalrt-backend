FROM gradle:6.3.0-jdk11 AS BUILDER
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
COPY . .
RUN gradle clean build

# actual container
FROM adoptopenjdk/openjdk11:alpine-jre
ENV APP_HOME=/usr/app

WORKDIR $APP_HOME
COPY --from=BUILDER $APP_HOME/build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
