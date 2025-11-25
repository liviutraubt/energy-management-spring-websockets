FROM maven:3.9.7-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /device_service/pom.xml
WORKDIR /device_service
RUN mvn dependency:go-offline
COPY ./src/ /device_service/src
WORKDIR /device_service
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /device_service
COPY --from=build /device_service/target/*.jar /device_service/device_service.jar
EXPOSE 8082
CMD ["java", "-jar", "device_service.jar"]
