FROM maven:3.9.7-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /loadbalancing_service/pom.xml
WORKDIR /loadbalancing_service
RUN mvn dependency:go-offline
COPY ./src/ /loadbalancing_service/src
WORKDIR /loadbalancing_service
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /loadbalancing_service
COPY --from=build /loadbalancing_service/target/*.jar /loadbalancing_service/loadbalancing_service.jar
EXPOSE 8086
CMD ["java", "-jar", "loadbalancing_service.jar"]
