FROM openjdk:17

LABEL org.opencontainers.image.documentation="https://github.com/itning/yunshu-nas/blob/master/README.md"
LABEL org.opencontainers.image.authors="itning@itning.top"
LABEL org.opencontainers.image.source="https://github.com/itning/yunshu-nas"
LABEL org.opencontainers.image.title="云舒NAS"
LABEL org.opencontainers.image.description="自建NAS系统，实现本地视频音频点播，文件存储等功能。自动视频转码，在线观看下载视频！"
LABEL org.opencontainers.image.licenses="Apache License 2.0"

ADD nas-deploy/target/yunshu-nas-*.RELEASE.jar /home/yunshu-nas.jar
# 端口暴露
EXPOSE 8888

ENTRYPOINT ["java","-jar","/home/yunshu-nas.jar"]