# Bước 1: Build ứng dụng
FROM maven:3.9.6-eclipse-temurin-17 AS build

# 1. Tạo thư mục làm việc (để không bị lẫn lộn ở thư mục gốc /)
WORKDIR /app

# 2. Copy file pom.xml vào trước
COPY pom.xml .

# 3. Copy toàn bộ source code (src) vào
COPY src ./src

# 4. Bây giờ mới chạy lệnh build
RUN mvn clean package -DskipTests

# -----------------------------------------------------
# Bước 2: Chạy ứng dụng (Runtime)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy file .jar từ bước build sang bước chạy (thay tên file jar cho đúng với project của bạn)
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-Xmx350m", "-jar", "app.jar"]