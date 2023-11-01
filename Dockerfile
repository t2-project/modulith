FROM eclipse-temurin:17-jre
WORKDIR /workspace/app
ENV PORT 8080
EXPOSE 8080
COPY target/*.war /workspace/app/app.war
ENTRYPOINT exec java $JAVA_OPTS -jar app.war
