FROM amazoncorretto:21
ARG JAR_FILE=build/libs/*.jar
ADD ${JAR_FILE} jmix-client.jar
ENTRYPOINT ["java","-jar","/jmix-client.jar"]