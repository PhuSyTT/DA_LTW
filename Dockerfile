# Stage 1: Build mã nguồn bằng Maven với Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy file cấu hình maven và mã nguồn
COPY pom.xml .
COPY src ./src

# Biên dịch và đóng gói ứng dụng (bỏ qua test để tăng tốc deploy)
RUN mvn clean package -DskipTests

# Stage 2: Môi trường chạy JRE 21 nhẹ và tối ưu RAM cho gói Free của Render
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy file jar đã đóng gói từ Stage 1
COPY --from=build /app/target/bookstore-chain-management-1.0.0.jar app.jar

# Render sẽ tự động truyền cổng qua biến môi trường $PORT (thường là 10000)
ENV PORT=8080
EXPOSE 8080

# Chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-Xmx384m", "-jar", "app.jar"]

