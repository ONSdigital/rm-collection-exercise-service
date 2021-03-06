FROM openjdk:11-jre-slim

RUN apt-get update
COPY target/collectionexercisesvc.jar /opt/collectionexercisesvc.jar

EXPOSE 8145
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]

