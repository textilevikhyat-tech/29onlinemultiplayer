# Use official Java 17 image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy ENTIRE project (not only server/)
COPY . .

# Move into server folder
WORKDIR /app/server

# Give permission to gradlew
RUN chmod +x ./gradlew

# Build the jar
RUN ./gradlew build --no-daemon

EXPOSE 10000

CMD ["sh", "-c", "java -cp build/libs/* com.mygame.server.Main"]
