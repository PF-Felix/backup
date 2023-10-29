# 配置用户信息

```shell
# global表示全局配置
git config --global user.name '用户名'
git config --global user.email '邮箱'
```

# 配置命令简写

``` 
[alias]
co = checkout
st = status
cma = commit --amend
cmm = commit -m
df = diff
br = branch
sta = stash
cp = cherry-pick
```

# 解决客户端乱码

```shell
# 解决 git bash 乱码
git config --global core.quotepath false
# 解决 gitk 乱码
git config --global gui.encoding utf-8
```

# 关闭换行符自动转换

`git config --global core.autocrlf false`

# branch

```shell
# 列出本地分支，-a 列出所有分支
git branch
# 切换到 abc 分支，如果没有就创建
git branch abc
# 查看本地分支与远程分支的关系
git branch -vv
# 创建本地分支，若没有对应远程分支可以不带远程分支参数
git checkout -b local_name orign_name
# 删除本地分支，-D 强制删除
git branch -d local_name
# 本地分支重命名
git branch -m old_name new_name
```

## 删除远程分支的方法

1. 修改本地分支名称
1. 删除远程分支
1. 推送本地分支到远程

```shell
git branch -m dev-230726 DEV230726
git push origin :dev-230726
git push origin DEV230726:DEV230726
```

## 合并多个远程分支的提交

https://blog.csdn.net/qq_21744873/article/details/82629343

# clone

```shell
# 默认下载master分支
git clone robin.hu@http://www.kernel.org/pub/scm/git/git.git
# -b name 指定远程分支名称
git clone -b masterName robin.hu@http://www.kernel.org/pub/scm/git/git.git
```

# push

```shell
git push
git push origin local_branch:remote_branch
```

# cherry-pick

`git cherry-pick 5464656465757`

# log

```shell
git log
# 查看某个分支的提交记录
git log [name]
# 精简显示
git log --oneline
# 显示最近3条记录
git log -3
# 按提交人搜索
git log --author=username
# 按关键字搜索
git log --grep keywords
# 按文件名搜索
git log -p -a/b/c/file.txt
```

# stash

```shell
# 暂存
git stash
# 查看/恢复/删除
git stash show/pop/drop
```

# merge

分支合并，下面举例将 abc 分支合并入 master 分支

```shell
git checkout master
git merge abc
```

# rebase

merge 可以合并分支，rebase 也可以做到

```shell
# 举例，将远程分支合并到当前分支
git fetch orign
git rebase 远程分支
git push -f
```

# diff

```shell
# 比较两个分支的代码差异
git diff dev..dev-v1.16.3.1105
```