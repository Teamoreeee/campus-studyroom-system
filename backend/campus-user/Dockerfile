# 校园自习室预约系统 - campus-user 运行镜像（使用预编译 jar）
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY backend/campus-user/target/*.jar app.jar

EXPOSE 8002

ENTRYPOINT ["java", "-jar", "app.jar"]
