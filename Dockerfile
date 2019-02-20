FROM openjdk:8-jre-slim

VOLUME /tmp
ARG JAR_FILE=collectionexercisesvc*.jar
RUN apt-get update
RUN apt-get -yq clean

RUN groupadd -g 991 collectionexercisesvc && \
    useradd -r -u 991 -g collectionexercisesvc collectionexercisesvc
USER collectionexercisesvc

COPY target/$JAR_FILE /opt/collectionexercisesvc.jar

ENTRYPOINT [ "java", "-jar", "/opt/collectionexercisesvc.jar" ]
