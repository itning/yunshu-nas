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
[![Hits](https://hitcount.itning.com?u=itning&r=yunshu-nas)](https://github.com/itning/hit-count)
[![language](https://img.shields.io/badge/language-JAVA-green.svg)](https://github.com/itning/yunshu-nas)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/itning/yunshu-nas/total)

</div>

---

# 依赖

jre = 21

# 支持数据库类型

MySQL8

Sqlite

Elasticsearch(可选) = 7

# Docker

镜像仓库地址：[![Docker Pulls](https://img.shields.io/docker/pulls/itning/yunshu-nas.svg?style=flat&label=pulls&logo=docker)](https://hub.docker.com/r/itning/yunshu-nas/tags?page=1&ordering=last_updated)

```shell script
docker run --name yunshu-nas -p 8888:8888 -e SERVER_URL=http://localhost:8888 itning/yunshu-nas:latest
```
其中环境变量`SERVER_URL=http://localhost:8888`意味着前端访问后端的地址是`http://localhost:8888`

# 启动脚本（aria2c 可以不用）

```shell script
nohup java -jar yunshu-nas.jar >log.log 2>&1 &
nohup aria2c --rpc-listen-port 6800 --enable-rpc --rpc-listen-all >aria2c.log 2>&1 &
```

启动后访问 `http://127.0.0.1:8888` 进入设置页面设置数据库及数据源配置。

# 实现功能
- [X] 点播视频文件

- [X] 点播音频文件 ~~[Angular版本](https://github.com/itning/YunShuMusicClient) [Electron版本](https://github.com/itning/YunShuMusicClientElectron)~~ [flutter版本](https://github.com/itning/yunshu_music) 支持WebDav path:/webdav

- [ ] 文件分布式存储

- [X] 远程下载

- [ ] 图片在线查看

- [X] 提供ftp服务

- [ ] 资料加密

- [ ] axel 下载支持

# 截图

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/a.png)

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/b.png)

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/c.png)

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/d.png)

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/e.png)

![](https://raw.githubusercontent.com/itning/yunshu-nas/master/pic/f.png)

# 感谢

![JetBrains Logo (Main) logo](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)
