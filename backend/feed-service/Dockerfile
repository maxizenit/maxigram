FROM amazoncorretto:21
ARG JAR_FILE=build/libs/*.jar
ADD ${JAR_FILE} feed-service.jar
ENTRYPOINT ["java","-jar","/feed-service.jar"]