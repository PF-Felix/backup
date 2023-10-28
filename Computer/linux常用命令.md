# uniq去重

uniq 可以去重，但仅仅是当相邻的数据重复时才会去重，比如`[1,3,3,2,3,4]`用 uniq 去重得到的是 `[1,3,2,3,4]`

想要真正的去重必须先排序，即`sort | uniq`，如下图

> 下图中 grep -Po 的意思是是只输出匹配的字符串

<img src="D:\ImageA\20231007095311.png" style="zoom: 43%;" />

想要得到重复的数量就得使用`uniq -c`，返回信息在 uniq 的结果前面加了一个数字表示数量，如下图

<img src="D:\ImageA\20231007095346.png" style="zoom:43%;" />

格式化的输出导致数字前面可能有空格，可以用`uniq -c | sed 's/^[[:space:]]*//'`去掉前面的空格

<img src="D:\ImageA\20231008112302.png" style="zoom:50%;" />

# grep匹配文本内容

```shell
grep "\[Frequency\]\[parse_success\]" /home/iiot/parse/log/*.parse.20230828.log
| grep -P "'gentime'\s*:\s*\'2023-08-28 17"
| grep -Po "'sid'\s*:\s*'\K[^']+"
```

上面语句能匹配到下面的日志：

<img src="D:\ImageA\image-20231014074516889.png" style="zoom: 43%;" />

**特殊符号的处理**

中括号是正则表达式的特殊符号，如果需要用必须加反斜线转义

**怎么匹配 "gentime": "2023-08-28 17-19-33.243424" 这样的字符串**~~类似的还有 "sid": "00-E0-E4-08-A9-CB"~~

需要考虑到冒号前后可能有几个空格的情况

答案如下，分别针对单引号和双引号的方案

```shell
grep -Po "'sid'\s*:\s*'\K[^']+"
grep -Po '"sid"\s*:\s*"\K[^']+'
```

<img src="D:\ImageA\20231008112335.png" style="zoom:50%;" />

最后只输出匹配到的字符串如下：

<img src="D:\ImageA\20231008112450.png" style="zoom:40%;" />

# 查看文件

**head/tail**

```shell
#查看文件开头/末尾10行
head/tail abc.txt
head/tail -10 abc.txt
#实时查看日志
tail -f abc.txt
```