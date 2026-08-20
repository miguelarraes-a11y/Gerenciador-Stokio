FROM eclipse-temurin:23-jdk AS build
WORKDIR /app

RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/apache-maven-3.9.8-bin.zip && \
    unzip apache-maven-3.9.8-bin.zip -d /opt && \
    ln -s /opt/apache-maven-3.9.8 /opt/maven && \
    ln -s /opt/maven/bin/mvn /usr/bin/mvn && \
    rm apache-maven-3.9.8-bin.zip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

COPY pom.xml .
RUN mvn dependency:go-offline -B || true

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:23-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]