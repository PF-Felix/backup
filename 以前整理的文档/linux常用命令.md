# 防火墙

```shell
# 查看防火墙状态
systemctl status firewalld
# 关闭防火墙
systemctl stop firewalld
# 查看防火墙服务是否开机启动
systemctl is-enabled firewalld
# 开启/关闭防火墙开机启动
systemctl enable/disable firewalld
```

# 查看本机信息

```shell
# 查看IP
ifconfig #需要net-tools
ip addr

# 查看主机名
hostname
cat /etc/hostname
# 修改主机名
hostnamectl set-hostname mycat-01
```

# vi

快捷键参考文章：
http://t.zoukankan.com/uriel-p-5788654.html
https://blog.csdn.net/Ljj9889/article/details/125839134

`i`进入编辑模式：

```shell
# 保存+退出
wq
# 不保存强制退出
q!
```

`ESC`进入选择模式：

```shell
# 删除当前光标所在字符
x
# 删除当前行（剪贴效果）
dd

# 在当前行下方插入新行
o
# 在当前行上方插入新行
shift+o

# 粘贴
p
# 清空
%d
# 显示行号
set nu

# 搜索
/xxx
# 查看下一个
n

# 跳到第一行/最后一行
:1
:$
```

# scp文件传输

https://www.runoob.com/linux/linux-comm-scp.html

```shell
# 复制文件
scp file1 root@10.0.0.20:/usr/local
# 复制目录
scp -r zookeeper root@10.0.0.20:/usr/local
```

# 压缩解压

```shell
unzip nacos-server-$version.zip
tar -xvf nacos-server-$version.tar.gz
```

# netstat

```shell
# 查看端口占用
netstat -tunlp | grep 端口号

# 安装netstat
yum -y install net-tools
```