FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY protos ./protos
COPY backend ./backend
RUN chmod +x backend/gradlew && cd backend && ./gradlew installDist --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=builder /app/backend/build/install/backend /app
EXPOSE 8080
HEALTHCHECK --interval=5s --timeout=3s --retries=10 CMD curl -sf http://localhost:8080/health || exit 1
CMD ["/app/bin/backend"]
