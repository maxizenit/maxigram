FROM amazoncorretto:21
ARG JAR_FILE=build/libs/*.jar
ADD ${JAR_FILE} user-service.jar
ENTRYPOINT ["java","-jar","/user-service.jar"]