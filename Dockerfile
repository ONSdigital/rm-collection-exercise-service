FROM openjdk:8-jre

ARG JAR_FILE=collectionexercisesvc*.jar
COPY target/$JAR_FILE /opt/collectionexercisesvc.jar

EXPOSE 8145
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]

