FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy only server folder content into container
COPY . .

# Make sure Gradle wrapper is executable
RUN chmod +x gradlew

# Build the jar using the Gradle wrapper
RUN ./gradlew build --no-daemon

EXPOSE 10000

CMD ["java", "-jar", "build/libs/server.jar"]
