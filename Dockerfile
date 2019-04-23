FROM java:8-jdk-alpine

VOLUME /tmp
COPY com.clms.typhonapi/target/gs-rest-service-0.1.0.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]