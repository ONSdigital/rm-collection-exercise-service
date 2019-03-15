FROM openjdk:8-jre-slim

VOLUME /tmp
ARG JAR_FILE=collectionexercisesvc*.jar
RUN apt-get update
RUN apt-get -yq clean

RUN groupadd --gid 991 collectionexercisesvc && \
    useradd --create-home --system --uid 991 --gid collectionexercisesvc collectionexercisesvc
USER collectionexercisesvc

COPY target/$JAR_FILE /opt/collectionexercisesvc.jar

ENTRYPOINT [ "java", "-jar", "/opt/collectionexercisesvc.jar" ]
