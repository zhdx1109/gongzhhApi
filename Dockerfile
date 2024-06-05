#设置镜像使用的基础镜像
FROM openjdk:8u322-jre-buster
# 作者
MAINTAINER zdx <376605531@qq.com>
#设置镜像暴露的端口 这里要与application.properties中的server.port保持一致
EXPOSE 80
#设置容器的挂载卷
VOLUME /tmp
#编译镜像时将springboot生成的jar文件复制到镜像中
COPY target/wx-api.jar  /wx-api.jar
#编译镜像时运行脚本
RUN apt-get update && apt-get install -y redis-server

# 创建 Redis 配置文件目录
RUN mkdir -p /etc/redis

# 设置 Redis 密码
RUN echo "requirepass 123456" >> /etc/redis/redis.conf
# 暴露端口
EXPOSE 80 6379
#容器的入口程序，这里注意如果要指定外部配置文件需要使用-spring.config.location指定配置文件存放目录
ENTRYPOINT ["bash", "-c", "redis-server /etc/redis/redis.conf & java -jar -Dspring.profiles.active=${PROFILE_ACTIVE:-dev} /wx-api.jar"]
