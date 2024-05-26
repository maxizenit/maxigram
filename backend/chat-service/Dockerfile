FROM amazoncorretto:21
ARG JAR_FILE=build/libs/*.jar
ADD ${JAR_FILE} chat-service.jar
ENTRYPOINT ["java","-jar","/chat-service.jar"]