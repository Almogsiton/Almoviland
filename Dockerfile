# Stage 1: Build the application using Ant
FROM eclipse-temurin:17-jdk-jammy AS build

# Install Ant and wget
RUN apt-get update && apt-get install -y ant wget

# Download Jakarta EE 10 API (required for compilation)
RUN mkdir -p /app/lib && \
    wget -O /app/lib/jakarta.jakartaee-api.jar https://repo1.maven.org/maven2/jakarta/platform/jakarta.jakartaee-api/10.0.0/jakarta.jakartaee-api-10.0.0.jar

# Set the working directory
WORKDIR /app

# Copy the project files
COPY . .

# Build the project using the custom simple build script
RUN ant -noinput -buildfile build-simple.xml dist

# Stage 2: Run the application in GlassFish
FROM ghcr.io/eclipse-ee4j/glassfish:latest

# Copy the WAR file from the build stage to the GlassFish autodeploy directory
COPY --from=build /app/dist/Almoviland.war ${GLASSFISH_HOME}/glassfish/domains/domain1/autodeploy/ROOT.war

# Ensure the default port is exposed (Render will map this externally)
EXPOSE 8080
