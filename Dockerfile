FROM openjdk:jdk-8
MAINTAINER Kieran Wardle <kieran.wardle@ons.gov.uk>
ARG jar
VOLUME /tmp
COPY $jar collexsvc.jar
RUN sh -c 'touch /collexsvc.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java -jar /collexsvc.jar" ]

