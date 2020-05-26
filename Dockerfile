FROM openjdk:8-jre-slim

RUN apt-get update
COPY target/collectionexercisesvc-UNVERSIONED.jar /opt/collectionexercisesvc.jar

EXPOSE 8145
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]

