FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy server folder content
COPY . .

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Build the project
RUN ./gradlew build --no-daemon

EXPOSE 10000

CMD ["sh", "-c", "java -jar build/libs/*.jar"]
