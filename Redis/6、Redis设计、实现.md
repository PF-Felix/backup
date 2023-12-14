# Redis的设计、实现

## 数据结构和内部编码

type命令实际返回的就是当前键的数据结构类型，它们分别是：string(字符串)hash(哈希)、list(列表)、set(集合)、zset (有序集合)，但这些只是Redis对外的数据结构。

实际上每种数据结构都有自己底层的内部编码实现，而且是多种实现，这样Redis会在合适的场景选择合适的内部编码。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1668158598097/d65322ec553941ba802899e1e0071d91.png)

每种数据结构都有两种以上的内部编码实现，例如list数据结构包含了linkedlist和ziplist两种内部编码。同时有些内部编码，例如ziplist,可以作为多种外部数据结构的内部实现，可以通过object encoding命令查询内部编码。

Redis这样设计有两个好处:

第一，可以改进内部编码，而对外的数据结构和命令没有影响，这样一旦开发出更优秀的内部编码，无需改动外部数据结构和命令，例如Redis3.2提供了quicklist，结合了ziplist和linkedlist两者的优势，为列表类型提供了一种更为优秀的内部编码实现，而对外部用户来说基本感知不到。

第二，多种内部编码实现可以在不同场景下发挥各自的优势，例如ziplist比较节省内存，但是在列表元素比较多的情况下，性能会有所下降，这时候Redis会根据配置选项将列表类型的内部实现转换为linkedlist。

### redisobject对象

Redis存储的所有值对象在内部定义为redisobject结构体，内部结构如图所示。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1668158598097/455898bf4c9b421c9bbcaa09ff7df7f8.png)

Redis存储的数据都使用redis0bject来封装，包括string、hash、list、set,zset在内的所有数据类型。理解redis0bject对内存优化非常有帮助，下面针对每个字段做详细说明:

#### type字段

type字段:表示当前对象使用的数据类型，Redis主要支持5种数据类型:string, hash、 list,set,zset。可以使用type { key}命令查看对象所属类型,type命令返回的是值对象类型,键都是string类型。

#### encoding字段

**encoding** **字段** :表示Redis内部编码类型,encoding在 Redis内部使用，代表当前对象内部采用哪种数据结构实现。理解Redis内部编码方式对于优化内存非常重要,同一个对象采用不同的编码实现内存占用存在明显差异。

#### lru字段

lru字段:记录对象最后次被访问的时间,当配置了maxmemory和maxmemory-policy=volatile-lru或者allkeys-lru时，用于辅助LRU算法删除键数据。可以使用object idletime {key}命令在不更新lru字段情况下查看当前键的空闲时间。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1668158598097/06878d251efb42fb95c55ea847c28cfe.png)

*可以使用scan +object idletime* *命令批量查询哪些键长时间未被访问，找出长时间不访问的键进行清理,* *可降低内存占用。*

#### refcount字段

refcount字段:记录当前对象被引用的次数，用于通过引用次数回收内存，当refcount=0时，可以安全回收当前对象空间。使用object refcount(key}获取当前对象引用。当对象为整数且范围在[0-9999]时，Redis可以使用共享对象的方式来节省内存。

PS面试题，Redis的对象垃圾回收算法-----引用计数法。

#### *ptr字段

*ptr字段:与对象的数据内容相关，如果是整数，直接存储数据;否则表示指向数据的指针。

Redis新版本字符串且长度<=44字节的数据，字符串sds和redisobject一起分配，从而只要一次内存操作即可。

*PS* *：高并发写入场景中，在条件允许的情况下，建议字符串长度控制在44**字节以内，减少创建redisobject**内存分配次数，从而提高性能。*

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1668158598097/de771c3770b242be8ca619252dd10c94.png)

## Redis中的线程和IO模型

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1668158598097/f7d5d5855fa2494e96154534adcb6acc.png)

Redis 基于 Reactor 模式开发了自己的网络事件处理器 - 文件事件处理器（file event handler，后文简称为 FEH），而该处理器又是单线程的，所以redis设计为单线程模型。

采用I/O多路复用同时监听多个socket，根据socket当前执行的事件来为socket 选择对应的事件处理器。

当被监听的socket准备好执行accept、read、write、close等操作时，和操作对应的文件事件就会产生，这时FEH就会调用socket之前关联好的事件处理器来处理对应事件。

所以虽然FEH是单线程运行，但通过I/O多路复用监听多个socket，不仅实现高性能的网络通信模型，又能和 Redis 服务器中其它同样单线程运行的模块交互，保证了Redis内部单线程模型的简洁设计。

下面来看文件事件处理器的几个组成部分。

#### socket

文件事件就是对socket操作的抽象， 每当一个 socket 准备好执行连接accept、read、write、close等操作时， 就会产生一个文件事件。一个服务器通常会连接多个socket，多个socket可能并发产生不同操作，每个操作对应不同文件事件。

#### I/O多路复用程序

I/O 多路复用程序会负责监听多个socket。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1668158598097/cdb769bb4c7649e0acd1bad68b4c0662.png)

#### 文件事件分派器

文件事件分派器接收 I/O 多路复用程序传来的socket， 并根据socket产生的事件类型， 调用相应的事件处理器。

#### 文件事件处理器

服务器会为执行不同任务的套接字关联不同的事件处理器， 这些处理器是一个个函数， 它们定义了某个事件发生时， 服务器应该执行的动作。

Redis 为各种文件事件需求编写了多个处理器，若客户端连接Redis，对连接服务器的各个客户端进行应答，就需要将socket映射到连接应答处理器写数据到Redis，接收客户端传来的命令请求，就需要映射到命令请求处理器从Redis读数据，向客户端返回命令的执行结果，就需要映射到命令回复处理器当主服务器和从服务器进行复制操作时，
主从服务器都需要映射到特别为复制功能编写的复制处理器。

### Redis6中的多线程



#### Redis6.0默认是否开启了多线程？

Redis6.0的多线程默认是禁用的，只使用主线程。如需开启需要修改redis.conf配置文件：io-threads-do-reads yes

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1668158598097/fe03fc71ee284640b3de6f6f54fb521b.png)

开启多线程后，还需要设置线程数，否则是不生效的。同样修改redis.conf配置文件

关于线程数的设置，官方有一个建议：4核的机器建议设置为2或3个线程，8核的建议设置为6个线程，线程数一定要小于机器核数。还需要注意的是，线程数并不是越大越好，官方认为超过了8个基本就没什么意义了。

