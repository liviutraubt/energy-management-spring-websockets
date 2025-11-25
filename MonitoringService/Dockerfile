FROM maven:3.9.7-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /monitoring_service/pom.xml
WORKDIR /monitoring_service
RUN mvn dependency:go-offline
COPY ./src/ /monitoring_service/src
WORKDIR /monitoring_service
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /monitoring_service
COPY --from=build /monitoring_service/target/*.jar /monitoring_service/monitoring_service.jar
EXPOSE 8084
CMD ["java", "-jar", "monitoring_service.jar"]
