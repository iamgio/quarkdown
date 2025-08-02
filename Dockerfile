# Build stage via Gradle
FROM gradle:8.14.3-jdk17 AS builder

COPY . /app
WORKDIR /app

# Build the distribution zip
# RUN gradle --no-daemon distZip

# For testing purposes, replace the Gradle build with the following to reduce delays.
RUN mkdir -p build/distributions
RUN curl -L -o build/distributions/quarkdown.zip https://github.com/iamgio/quarkdown/releases/download/v1.8.0/quarkdown.zip

WORKDIR build/distributions
RUN unzip quarkdown.zip && rm quarkdown.zip

# Run stage
# FROM ghcr.io/puppeteer/puppeteer:latest AS runner
FROM gradle:8.14.3-jdk17 AS runner

WORKDIR /app
COPY --from=builder /app/build/distributions/quarkdown quarkdown
ENV PATH="/app/quarkdown/bin:${PATH}"

ENTRYPOINT ["quarkdown"]