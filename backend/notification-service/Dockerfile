FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"
EXPOSE 8088
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
