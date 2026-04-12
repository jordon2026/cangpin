# 使用 Amazon Corretto 17 作为基础镜像
FROM amazoncorretto:17-alpine

# 设置工作目录
WORKDIR /app

# 安装必要的工具和中文字体（验证码需要）
RUN apk add --no-cache curl fontconfig ttf-dejavu

# 复制 Maven 构建的 jar 文件
COPY target/museum-server-1.0.0-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
