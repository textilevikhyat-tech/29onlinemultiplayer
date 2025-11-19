# Use official Java 17 image from Eclipse Temurin
FROM eclipse-temurin:17-jdk

# Install Gradle
RUN apt-get update && apt-get install -y wget unzip \
    && wget https://services.gradle.org/distributions/gradle-7.6-bin.zip -P /tmp \
    && unzip -d /opt/gradle /tmp/gradle-7.6-bin.zip \
    && rm /tmp/gradle-7.6-bin.zip

ENV GRADLE_HOME=/opt/gradle/gradle-7.6
ENV PATH=${GRADLE_HOME}/bin:${PATH}

# Set working directory
WORKDIR /app

# Copy server folder content into container
COPY . .

# Build server using installed Gradle
RUN gradle build --no-daemon --stacktrace


# Expose port (Render assigns actual PORT)
EXPOSE 10000

# Start server; use wildcard jar in build/libs
CMD ["sh", "-c", "java -cp build/libs/* com.mygame.server.Main"]
