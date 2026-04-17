FROM maven:3.9.9-amazoncorretto-17 AS build

WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

# Fix CRLF line endings và cấp quyền execute cho mvnw
RUN sed -i 's/\r//' mvnw && chmod +x mvnw

RUN mvn package -DskipTests

# Runtime image
FROM amazoncorretto:17.0.12-alpine3.17
WORKDIR /app

# Copy jar từ stage build
COPY --from=build /app/target/*.jar app.jar

# Tạo thư mục upload và /data
RUN mkdir -p /app/upload /data

# Mount volume để giữ dữ liệu
VOLUME ["/data", "/app/upload"]

ENTRYPOINT ["java", "-jar", "app.jar"]