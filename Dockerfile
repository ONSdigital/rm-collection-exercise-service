FROM openjdk
ARG jar
VOLUME /tmp
ADD $jar collexsvc.jar
RUN sh -c 'touch /collexsvc.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java -jar /collexsvc.jar" ]

