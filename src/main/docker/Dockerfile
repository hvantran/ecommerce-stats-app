FROM openjdk:21-jdk-oracle as builder
# First stage : Extract the layers
WORKDIR application
COPY ${project.build.finalName}.jar ${project.build.finalName}.jar
RUN java -Djarmode=layertools -jar ${project.build.finalName}.jar extract

FROM openjdk:21-jdk-oracle as runtime
# Second stage : Copy the extracted layers
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
COPY elastic-apm-agent-1.44.0.jar elastic-apm-agent-1.44.0.jar
CMD ["java", "-javaagent:elastic-apm-agent-${apm.agent.version}.jar","org.springframework.boot.loader.launch.JarLauncher"]