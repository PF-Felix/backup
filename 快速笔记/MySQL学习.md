Windows 里没有像类 UNIX 系统中那么多的启动脚本，但是也提供了手动启动和以服务的形式启动这两种方式

MySQL 安装目录下的 bin 目录下有一个 mysqld 可执行文件，在命令行里输入 mysqld ，或者直接双 击运行它就算启动了 MySQL 服务器程序了

也可以把它注册为服务："C:\Program Files\MySQL\MySQL Server 5.7\bin\mysqld" --install
net start MySQL 可以启动服务
net stop MySQL

mysqld 是服务端程序，而 mysql 是客户端程序

mysql -h localhost -u root -p123456

其实不论客户端进程和服务器进程是采用哪种方式进行通信，最后实现的效果都是：客户端进程向服务器进程发 送一段文本（MySQL语句），服务器进程处理后再向客户端进程发送一段文本（处理结果）。

<img src="D:\FigureBed\image-20230904142013531.png" alt="image-20230904142013531" style="zoom: 33%;" />

连接管理：每当有一个客户端进程连接到服务器进程时，服务器进程都会创建一个线程来专门处理与这个 客户端的交互，当该客户端退出时会与服务器断开连接，服务器并不会立即把与该客户端交互的线程销毁掉，而 是把它缓存起来，在另一个新的客户端再进行连接时，把这个缓存的线程分配给该新客户端。这样就起到了不频 繁创建和销毁线程的效果，从而节省开销。从这一点大家也能看出， MySQL 服务器会为每一个连接进来的客户端 分配一个线程，但是线程分配的太多了会严重影响系统性能，所以我们也需要限制一下可以同时连接到服务器的 客户端数量，至于怎么限制我们后边再说哈～ **连接池**

**查询缓存** MySQL会把刚刚处理过的查询请求和结果 缓存 起来，如果下一次有一模一样的请求过来，直接从缓存中查找结果就好了这样更快

这个查询缓存可以在不同客户端之间共享，不区分访问者，只区分查询的字符串

但是如果两个查询请求在任何字符上的不同（例如：空格、注释、大小写），都会导致缓存不会命中

另外，如果查询请求中包含某些系统函数、用户自定义变量和函数、一些系统表，如 mysql 、information_schema、 performance_schema 数据库中的表，那这个请求就不会被缓存。比如函数 NOW ，每次调用都会产生最新的当前时间， 如果在一个查询请求中调用了这个函数，那即使查询请求的文本信息都一样，那不同时间的两次查询也应该得到 不同的结果，如果在第一次查询时就缓存了，那第二次查询的时候直接使用第一次查询的结果就是错误的！

不过既然是缓存，那就有它缓存失效的时候。MySQL的缓存系统会监测涉及到的每张表，只要该表的结构或者数 据被修改，如对该表使用了 INSERT 、 UPDATE 、 DELETE 、 TRUNCATE TABLE 、 ALTER TABLE 、 DROP TABLE 或 DROP DATABASE 语句，那使用该表的所有高速缓存查询都将变为无效并从高速缓存中删除！ 

虽然查询缓存有时可以提升系统性能，但也不得不因维护这块缓存而造成一些开销，比如每次都要去查 询缓存中检索，查询请求处理完需要更新查询缓存，维护该查询缓存对应的内存区域。从MySQL 5.7.20 开始，不推荐使用查询缓存，并在MySQL 8.0中删除。

**语法解析**跳过

**查询优化**

先跳过

**存储引擎**

负责管理数据

<img src="D:\FigureBed\image-20230904143042215.png" alt="image-20230904143042215" style="zoom:35%;" />

我们之前创建表的语句都没有指定表的存储引擎，那就会使用默认的存储引擎 InnoDB （当然这个默认的存储引 擎也是可以修改的，我们在后边的章节中再说怎么改）。如果我们想显式的指定一下表的存储引擎，那可以这么 写：

```sql
CREATE TABLE `script` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(50) NOT NULL COMMENT '名称',
    `content` text NOT NULL COMMENT '内容',
    `deleted` int(20) unsigned NOT NULL DEFAULT '0' COMMENT '无用字段，此表采用物理删除',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '修改时间',
    `create_user` varchar(50) NOT NULL COMMENT '创建人',
    `update_user` varchar(50) NOT NULL COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='脚本表';

ALTER TABLE `script` ENGINE = MyISAM;
```

**启动参数 与 配置文件**

启动命令类似`mysqld --default-storage-engine=MyISAM`

`--启动选项1[=值1] --启动选项2[=值2] ... --启动选项n[=值n]`

在命令行中设置启动选项只对当次启动生效，也就是说如果下一次重启程序的时候我们还想保留这些启动选项的 话，还得重复把这些选项写到启动命令行中！

于是有了配置文件的概念，把需要设置的启动选项都写在这个配置文件中，每次启动服务器的时候都从 这个文件里加载相应的启动选项。推荐使用

<img src="D:\FigureBed\image-20230904144124263.png" alt="image-20230904144124263" style="zoom:38%;" />

<img src="D:\FigureBed\image-20230904144250620.png" alt="image-20230904144250620" style="zoom:40%;" />

<img src="D:\FigureBed\image-20230904144706450.png" alt="image-20230904144706450" style="zoom:50%;" />

优先级：MySQL 将按照我们在上表中给定的顺序依次读取各个配置文件，如 果该文件不存在则忽略。值得注意的是，如果我们在多个配置文件中设置了相同的启动选项，那以最后一个配置 文件中的为准

同理：同一个参数如果在多个组都有配置，后配置的将覆盖前面配置的

如果我们不想让 MySQL 到默认的路径下搜索配置文件（就是上表中列出的那些），可以在命令行指定 defaults-file 选项

mysqld --defaults-file=/tmp/myconfig.txt

另外：如果同一个启动选项既出现在命令行中，又出现在配置文件中，那么以命令行中的启 动选项为准

**系统变量**

SHOW VARIABLES [LIKE 匹配的模式];

SHOW VARIABLES LIKE 'default_storage_engine';

SHOW VARIABLES like 'max_connections';

SHOW VARIABLES LIKE 'default%';

系统变量可以作为启动项设置，就是上文提到的方法

系统变量 比较牛逼的一点就是，对于大部分系统变量来说，它们的值可以在服务器程序运行过程中进行动态修 改而无需停止并重启服务器。

每个客户端改的话只会改它的系统变量，对其他客户端没有影响，相当于全局设置没变，是在会话级别改变的。

很显然，通过启动选项设置的系统变量的作用范围都是 GLOBAL 的，也就是对所有客户端都有效的

<img src="D:\FigureBed\image-20230904150516345.png" alt="image-20230904150516345" style="zoom:33%;" />

<img src="D:\FigureBed\image-20230904150542032.png" alt="image-20230904150542032" style="zoom:33%;" />

而上面 SHOW VARIABLES 查看的是 session 范围的变量