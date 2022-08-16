FROM openjdk:17-jdk-alpine
ADD ${project.build.finalName}.jar ${project.build.finalName}.jar
ADD https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/1.33.0/elastic-apm-agent-1.33.0.jar elastic-apm-agent-1.33.0.jar
ENTRYPOINT ["java", "-javaagent:elastic-apm-agent-${apm.agent.version}.jar", "-jar", "${project.build.finalName}.jar"]