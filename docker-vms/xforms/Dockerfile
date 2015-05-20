FROM maven:3.3-jdk-7
MAINTAINER Praekelt Foundation <dev@praekeltfoundation.org>

EXPOSE 8080 8081

RUN git clone https://github.com/praekelt/xforms-service.git
COPY . /xforms-service
WORKDIR /xforms-service

RUN mvn install:install-file -Dfile=lib/javarosa.jar -DgroupId=org.javarosa -DartifactId=javarosa-libraries -Dversion=latest -Dpackaging=jar -DgeneratePom=true
RUN mvn clean package -Dmaven.test.skip=true

CMD ["java","-jar","target/restforms-1.0-SNAPSHOT.jar","server","configuration.yaml"]
