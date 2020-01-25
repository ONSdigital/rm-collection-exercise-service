FROM openjdk:8-jre-slim

RUN groupadd --gid 999 collectionexercisesvc && \
    useradd --create-home --system --uid 999 --gid collectionexercisesvc collectionexercisesvc
USER collectionexercisesvc

COPY collectionexercisesvc /opt/collectionexercisesvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/collectionexercisesvc.jar" ]

