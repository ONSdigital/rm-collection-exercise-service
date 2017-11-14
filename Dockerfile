FROM openjdk:8-jre

COPY target/collectionexercisesvc*.jar /opt/collectionexercisesvc.jar

ENTRYPOINT [ "java", "-jar", "/opt/collectionexercisesvc.jar" ]

