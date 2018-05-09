FROM openjdk:8-jre-slim

ARG JAR_FILE=collectionexercisesvc*.jar
RUN apt-get update
RUN apt-get -yq install curl
RUN apt-get -yq clean
COPY target/$JAR_FILE /opt/collectionexercisesvc.jar

EXPOSE 8145
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]

