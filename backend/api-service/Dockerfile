FROM amazoncorretto:21
ARG JAR_FILE=build/libs/*.jar
ADD ${JAR_FILE} api-service.jar
ENTRYPOINT ["java","-jar","/api-service.jar"]