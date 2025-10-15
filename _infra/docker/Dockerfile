FROM eclipse-temurin:21-jre-alpine

COPY ./cacert.pem /usr/local/share/ca-certificates/cacert.pem
RUN update-ca-certificates

RUN apk update
COPY target/collectionexercisesvc.jar /opt/collectionexercisesvc.jar

EXPOSE 8145
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]
