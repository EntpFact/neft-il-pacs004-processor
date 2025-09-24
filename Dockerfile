FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.21-1.1739376167
COPY build/libs/neft-il-pacs004-processor-0.0.1-SNAPSHOT.jar /neft-il-pacs004-processor-0.0.1.jar
ENTRYPOINT ["java","-jar","/neft-il-pacs004-processor-0.0.1.jar"]
