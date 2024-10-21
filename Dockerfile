FROM khipu/openjdk21-alpine
VOLUME /tmp
ARG JAR_FILE
ADD target/ParkingDataIt4-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
RUN apk add --update tzdata
ENV TZ=America/Guatemala
RUN echo ${TZ} > /etc/timezone
ENV LANG es_GT.UTF-8
ENV LANGUAGE es_GT.UTF-8
ENV LC_ALL es_GT.UTF-8
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]