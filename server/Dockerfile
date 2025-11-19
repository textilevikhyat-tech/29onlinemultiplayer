# Use official Java 17 image from Eclipse Temurin
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy all server files into container
COPY . .

# Build the server
RUN ./gradlew build

# Expose port (Render will assign actual PORT)
EXPOSE 10000

# Start server; use wildcard jar in build/libs
CMD ["sh", "-c", "java -cp build/libs/* com.mygame.server.Main"]
