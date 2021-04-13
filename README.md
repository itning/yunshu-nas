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
[![HitCount](http://hits.dwyl.io/itning/yunshu-nas.svg)](http://hits.dwyl.io/itning/yunshu-nas)
[![language](https://img.shields.io/badge/language-JAVA-green.svg)](https://github.com/itning/yunshu-nas)

</div>

---

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

- [X] 点播音频文件 [Angular版本](https://github.com/itning/YunShuMusicClient) [Electron版本](https://github.com/itning/YunShuMusicClientElectron)

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