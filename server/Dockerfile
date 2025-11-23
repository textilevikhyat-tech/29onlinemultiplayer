FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy all project files
COPY . .

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Build the project
RUN ./gradlew build --no-daemon

EXPOSE 10000

# Auto-detect the MAIN runnable jar (excluding *plain.jar)
CMD ["sh", "-c", "JAR_FILE=$(ls build/libs | grep -v plain | head -n 1) && \
     echo \"Running: $JAR_FILE\" && \
     java -jar build/libs/$JAR_FILE --server.port=$PORT"]
