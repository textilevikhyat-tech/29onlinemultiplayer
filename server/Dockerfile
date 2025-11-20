FROM eclipse-temurin:17-jdk

# Install Gradle
RUN apt-get update && apt-get install -y wget unzip \
    && wget https://services.gradle.org/distributions/gradle-7.6-bin.zip -P /tmp \
    && unzip -d /opt/gradle /tmp/gradle-7.6-bin.zip \
    && rm /tmp/gradle-7.6-bin.zip

ENV GRADLE_HOME=/opt/gradle/gradle-7.6
ENV PATH=${GRADLE_HOME}/bin:${PATH}

WORKDIR /app
COPY . .

RUN gradle build --no-daemon

EXPOSE 10000

CMD ["sh", "-c", "java -jar server/build/libs/*"]
