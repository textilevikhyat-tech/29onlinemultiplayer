# Use official Java 17 image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy Gradle wrapper + build files first (for caching)
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Give permission to gradlew
RUN chmod +x gradlew

# Download dependencies (better caching)
RUN ./gradlew --no-daemon dependencies

# Copy project source
COPY src ./src

# Build Spring Boot JAR
RUN ./gradlew build --no-daemon

# Expose port (Render injects $PORT automatically)
EXPOSE 8080

# Run Spring Boot JAR
CMD ["sh", "-c", "java -jar build/libs/*.jar"]
