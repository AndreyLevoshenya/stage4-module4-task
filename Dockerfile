# ===== STAGE 1: Build frontend =====
FROM node:18-alpine as frontend
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

# ===== STAGE 2: Build backend =====
FROM gradle:8.7-jdk17 as backend
WORKDIR /app
COPY backend/ .
COPY --from=frontend /frontend/build /app/module-web/src/main/resources/static
RUN gradle clean bootJar -x test --no-daemon

# ===== STAGE 3: Build app =====
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=backend /app/module-web/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
