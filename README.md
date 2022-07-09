<h3 align="center">云舒NAS</h3>
<div align="center">

[![GitHub stars](https://img.shields.io/github/stars/itning/yunshu-nas.svg?style=social&label=Stars)](https://github.com/itning/yunshu-nas/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/itning/yunshu-nas.svg?style=social&label=Fork)](https://github.com/itning/yunshu-nas/network/members)
[![GitHub watchers](https://img.shields.io/github/watchers/itning/yunshu-nas.svg?style=social&label=Watch)](https://github.com/itning/yunshu-nas/watchers)
[![GitHub followers](https://img.shields.io/github/followers/itning.svg?style=social&label=Follow)](https://github.com/itning?tab=followers)


</div>

<div align="center">

[![Java CI with Maven](https://github.com/itning/yunshu-nas/actions/workflows/maven.yml/badge.svg)](https://github.com/itning/yunshu-nas/actions/workflows/maven.yml)
[![GitHub issues](https://img.shields.io/github/issues/itning/yunshu-nas.svg)](https://github.com/itning/yunshu-nas/issues)
[![GitHub license](https://img.shields.io/github/license/itning/yunshu-nas.svg)](https://github.com/itning/yunshu-nas/blob/master/LICENSE)
[![GitHub last commit](https://img.shields.io/github/last-commit/itning/yunshu-nas.svg)](https://github.com/itning/yunshu-nas/commits)
[![GitHub release](https://img.shields.io/github/release/itning/yunshu-nas.svg)](https://github.com/itning/yunshu-nas/releases)
[![GitHub repo size in bytes](https://img.shields.io/github/repo-size/itning/yunshu-nas.svg)](https://github.com/itning/yunshu-nas)
[![Hits](https://hitcount.itning.top?u=itning&r=yunshu-nas)](https://github.com/itning/hit-count)
[![language](https://img.shields.io/badge/language-JAVA-green.svg)](https://github.com/itning/yunshu-nas)

</div>

---

# Docker

镜像仓库地址：[DockerHub-itning](https://hub.docker.com/r/itning/yunshu-nas/tags?page=1&ordering=last_updated)

```shell script
docker run -d -p 8888:8888 -e MYSQL_URL=mysql8 -e MYSQL_PORT=3306 -e MYSQL_USERNAME=root -e MYSQL_PASSWORD=root --name yunshu-nas itning/yunshu-nas
```

| 环境变量                        | 用途                        | 默认值                                                       |
| ------------------------------- | --------------------------- | ------------------------------------------------------------ |
| MYSQL_URL                       | MySQL的地址（不包含端口号） | localhost                                                    |
| MYSQL_PORT                      | MySQL的端口号               | 3306                                                         |
| MYSQL_USERNAME                  | MySQL用户名                 | root                                                         |
| MYSQL_PASSWORD                  | MySQL密码                   | root                                                         |
| NAS_FFMPEG_BIN_DIR              | ffmpeg bin 目录位置         | /home/ffmpeg/bin                                             |
| NAS_OUT_DIR                     | 转码目录位置                | /home/tmp                                                    |
| NAS_ARIA2C_FILE                 | aria2c.exe 文件位置         | 空                                                           |
| NAS_MUSIC_DIR                   | 音乐文件目录                | /home/music_yunshu                                           |
| NAS_LYRIC_DIR                   | 歌词文件目录                | /home/lyric_yunshu                                           |
| nas.file-data-source-url-prefix | 文件数据源URL前缀           | 影响音乐API返回结果，例如配置：http://example.com 则返回音乐URL为：http://example.com/file?id=abc |
| nas.enable-basic-auth           | 是否开启basic基础认证       | 默认false 不开启                                             |
| nas.basic-auth-username         | basic基础认证用户名         | basic基础认证用户名                                          |
| nas.basic-auth-password         | basic基础认证密码           | basic基础认证密码                                            |
| nas.ignore-pat                  | basic基础认证忽略路径       | 多个路径使用英文逗号分隔                                     |

# 启动脚本（aria2c 可以不用）

```shell script
nohup java -jar yunshu-nas-0.0.1-SNAPSHOT.jar --nas.ffmpeg-bin-dir=/home/shw/ffmpeg-4.2.1-amd64-static --nas.out-dir=/home/shw/a --nas.aria2c-file=/usr/local/bin/aria2c >log.log 2>&1 &
nohup aria2c --rpc-listen-port 6800 --enable-rpc --rpc-listen-all >aria2c.log 2>&1 &
```
| 属性               | 含义             | 例子                                                     |
| ------------------ | ---------------- | -------------------------------------------------------- |
| nas.ffmpeg-bin-dir | Ffmpeg 所在目录  | --nas.ffmpeg-bin-dir=/home/shw/ffmpeg-4.2.1-amd64-static |
| nas.out-dir        | HLS视频输出目录  | --nas.out-dir=/home/shw/a                                |
| nas.aria2c-file    | aria2c文件全路径 | --nas.aria2c-file=/usr/local/bin/aria2c                  |
| nas.music-file-dir | 音乐文件目录 | --nas.music-file-dir=/home/music           |

# 实现功能
- [X] 点播视频文件

- [X] 点播音频文件 ~~[Angular版本](https://github.com/itning/YunShuMusicClient) [Electron版本](https://github.com/itning/YunShuMusicClientElectron)~~ [flutter版本](https://github.com/itning/yunshu_music)

- [ ] 文件分布式存储

- [X] 远程下载

- [ ] 图片在线查看

- [ ] 提供ftp服务

- [ ] 资料加密

- [ ] axel 下载支持

# 截图

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/a.png)

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/b.png)

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/c.png)

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/d.png)

# 感谢

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/jetbrains.png)
