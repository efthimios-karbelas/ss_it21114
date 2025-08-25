FROM maven:3.9-eclipse-temurin-24

WORKDIR /it21114

CMD ["mvn","-q","spring-boot:run","-Dspring-boot.run.profiles=dev","-DskipTests"]
