FROM openjdk:17.0.2

MAINTAINER itning itning@itning.top

ADD nas-deploy/target/yunshu-nas-*.RELEASE.jar /home/yunshu-nas.jar
# 端口暴露
EXPOSE 8888

ENTRYPOINT ["java","-jar","/home/yunshu-nas.jar"]