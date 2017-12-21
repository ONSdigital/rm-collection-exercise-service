ARG JAR_FILE=collectionexercisesvc*.jar
FROM openjdk:8-jre

ARG JAR_FILE
COPY target/$JAR_FILE /opt/collectionexercisesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]

