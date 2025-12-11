FROM maven:3.9.7-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /chat_service/pom.xml
WORKDIR /chat_service
RUN mvn dependency:go-offline
COPY ./src/ /chat_service/src
WORKDIR /chat_service
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /chat_service
COPY --from=build /chat_service/target/*.jar /chat_service/chat_service.jar
EXPOSE 8085
CMD ["java", "-jar", "chat_service.jar"]
