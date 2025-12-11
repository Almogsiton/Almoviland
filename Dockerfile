# Stage 1: Build the application using Ant
FROM frekele/ant:1.10.14-jdk17 AS build

# Set the working directory
WORKDIR /app

# Copy the project files
COPY . .

# Build the project (creates the WAR file in dist/)
RUN ant -noinput -buildfile build.xml dist

# Stage 2: Run the application in GlassFish
FROM ghcr.io/eclipse-ee4j/glassfish:latest

# Copy the WAR file from the build stage to the GlassFish autodeploy directory
COPY --from=build /app/dist/Almoviland.war ${GLASSFISH_HOME}/glassfish/domains/domain1/autodeploy/

# Ensure the default port is exposed (Render will map this externally)
EXPOSE 8080
