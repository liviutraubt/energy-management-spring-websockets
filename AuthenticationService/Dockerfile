FROM maven:3.9.7-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /authentication_service/pom.xml
WORKDIR /authentication_service
RUN mvn dependency:go-offline
COPY ./src/ /authentication_service/src
WORKDIR /authentication_service
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /authentication_service
COPY --from=build /authentication_service/target/*.jar /authentication_service/authentication_service.jar
EXPOSE 8083
CMD ["java", "-jar", "authentication_service.jar"]
