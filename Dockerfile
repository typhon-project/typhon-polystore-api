FROM openjdk:11-jre-slim


VOLUME /tmp
COPY com.clms.typhonapi/target/typhon-polystore-api-0.1.0.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
#RUN apk add --no-cache bash
#RUN apk add --no-cache mysql-client
RUN apt-get -y update \
    && apt-get -y install wait-for-it \
    && apt-get -y autoremove \
    && apt-get -y autoclean \
    && apt-get -y clean \
    && rm -rf /var/lib/apt/lists/*

CMD ["/bin/bash"]
