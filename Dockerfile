# Build command: docker build -t blk-hacking-ind-retirement-system .
# This Dockerfile uses Ubuntu (a Linux distribution) for stability and wide ecosystem support

FROM ubuntu:22.04 AS builder

# Install Java 17
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM ubuntu:22.04

# Install Java 17 runtime only (smaller image)
RUN apt-get update && \
    apt-get install -y openjdk-17-jre && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose port 5477
EXPOSE 5477

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -cp app.jar com.blackrock.retirement.RetirementSystemApplication || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
