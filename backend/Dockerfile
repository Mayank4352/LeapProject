FROM openjdk:17-jdk-slim

# Install Maven
RUN apt-get update && apt-get install -y maven && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY backend/pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY backend/src src

# Build the application
RUN mvn clean package -DskipTests

# Run the application
EXPOSE 8080
CMD ["java", "-jar", "target/ticketing-system-0.0.1-SNAPSHOT.jar"]
