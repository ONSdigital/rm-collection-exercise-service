FROM eclipse-temurin:21-jre-alpine

COPY ./Cisco_Umbrella_Root_CA.pem /usr/local/share/ca-certificates/Cisco_Umbrella_Root_CA.pem
RUN update-ca-certificates

RUN apk update
COPY target/collectionexercisesvc.jar /opt/collectionexercisesvc.jar

EXPOSE 8145
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]
