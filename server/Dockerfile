FROM eclipse-temurin:17-jdk

WORKDIR /app

# copy server folder contents into container
COPY . .

# Make gradlew executable if present; otherwise assume wrapper exists
RUN chmod +x gradlew || true

# Build the jar
RUN ./gradlew build --no-daemon

EXPOSE 10000

# run jar
CMD ["sh", "-c", "java -jar build/libs/online29server-0.1.0.jar"]
