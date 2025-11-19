# Use official Java image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy everything from server folder to /app in container
COPY . .

# Build the Java server
RUN ./gradlew build

# Expose port
EXPOSE 10000

# Start server using environment PORT if available
CMD ["sh", "-c", "java -cp build/libs/* com.mygame.server.Main"]
