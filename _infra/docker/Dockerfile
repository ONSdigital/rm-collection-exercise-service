FROM openjdk:8-jre-slim

COPY collectionexercisesvc-UNVERSIONED.jar /opt/collectionexercisesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]

