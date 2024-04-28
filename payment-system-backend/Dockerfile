FROM amazoncorretto:17
ARG JAR_FILE='/build/libs/payment-system-backend-0.0.1-SNAPSHOT.jar'
COPY ${JAR_FILE} application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]