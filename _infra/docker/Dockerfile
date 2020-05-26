FROM openjdk:8-jre-slim

COPY collectionexercisesvc.jar /opt/collectionexercisesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]

