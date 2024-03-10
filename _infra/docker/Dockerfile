FROM eclipse-temurin:17-jre-alpine

RUN apk update
COPY target/collectionexercisesvc.jar /opt/collectionexercisesvc.jar

EXPOSE 8145
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]

