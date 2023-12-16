# 消息模型&架构模型&核心组件

## 消息模型

![](C:\backup\assets\ebcf3458d04b36f47f4c9633c1e36bf7.png)

## 架构模型

![](C:\backup\assets\ee0435f80da5faecf47bca69b1c831cb.png)

## 核心组件

![image-20230428222615589](C:\backup\assets\image-20230428222615589.png)

==NameServer==
命名服务，注册中心
管理 Broker 节点，包括路由注册、路由更新、路由删除等
生产者在发送消息之前先从 NameServer 获取 Broker 服务器地址列表，然后根据负载均衡算法选择一台服务器进行消息发送
消费者在订阅某主题的消息之前从 NamerServer 获取 Broker......，从 Broker 中订阅消息，订阅规则由 Broker 配置决定

==Producer/Consumer==
消息生产者和消费者
生产者支持同步发送、异步发送、顺序发送、单向发送
消费者支持拉取式消费、推动式消费，但是本质上都是从 Broker 拉消息
消费者负载均衡模式有两种：集群模式和广播模式

==Broker==
接收 Producer 和 Consumer 的请求，调用 Store 层服务对消息进行处理
高可用服务的基本单元，支持同步双写、异步双写等模式

==Store==
存储层实现，同时包括了索引服务，高可用服务实现

==Netty Remoting Server/Netty Remoting Client==
基于 netty 的底层通信实现，所有服务间的交互都基于此模块
也区分服务端和客户端

# NameServer

## 启动流程

![image-20230429003406257](C:\backup\assets\image-20230429003406257.png)

==NamesrvStartup.java==

```java
/**
 * CODE-GPF
 * NameServer启动流程
 * ----- createNamesrvController -----
 * 1、创建配置
 * 2、设置监听端口号9876 写死了
 * 3、解析-c启动参数 将参数存储到创建的配置中
 * 4、解析-p启动参数 启动参数中有 -p 就可以打印这个 NameServer 的所有参数信息，打印之后退出，NameServer 不会启动
 *     除了这里可以打印启动参数之外，正常启动之后在 NameServer 日志里也可以看到所有的启动参数
 * 5、将启动参数填充到配置
 * 6、利用前面创建的配置置创建 NamesrvController 对象
 * ----- NamesrvController#initialize -----
 * 7、加载KV配置
 * 8、创建 NettyRemotingServer 对象
 * 9、【线程池】【路由剔除】【实现了高可用】每隔 10s 遍历一次活跃的 Broker 列表
 *     如果某个 Broker 上次上报心跳的时间距离当前时间超过120s 就认为它失效了，关闭连接 + 剔除Broker + 更新路由信息
 * 10、【线程池】每隔 10min 打印一次KV配置
 * ----- NamesrvController#start -----
 * 11、NettyRemotingServer 对象启动，用来接收 Broker 的路由、心跳信息【路由注册】
 */
public static NamesrvController main0(String[] args) {
}
```

## 源码分析亮点：读锁&写锁提高并发性能

参考《核心组件》
![image-20230429012743520](C:\backup\assets\image-20230429012743520.png)

关键类：RouteInfoManager
![image-20230429013227751](C:\backup\assets\image-20230429013227751.png)

对于 NameServer
1、生产者和消费者会频繁读
2、写的情况相对比较少：服务注册、服务删除等
读锁和写锁分开，保证并发读写的前提下提升效率，适用于读多写少的场景

## 源码亮点分析：NameServer无状态化

> 无状态服务：不会在本地存储持久化数据，多个实例对于同一个用户请求的响应结果是完全一致的，多服务实例之间没有依赖关系，比如 k8s 中的 Pod，扩容缩容对集群整体无影响
>
> 有状态服务：需要在本地存储持久化数据，典型的是分布式数据库，分布式节点实例之间有依赖的拓扑关系
> 比如主从，如果 k8s 停止分布式集群中任一实例 Pod，就可能导致数据丢失或者集群不可用

NS 是无状态的
1、基于内存的，不持久化数据（如下图）
2、NameServer 集群中它们相互之间不通讯，无依赖关系，设计简单，代码轻量

![image-20230429013421860](C:\backup\assets\image-20230429013421860.png)

# Broker

## 启动流程

![image-20230429115134381](C:\backup\assets\image-20230429115134381.png)

==BrokerStartup.java==

```java
/**
 * PS：commandLine启动参数相关的逻辑忽略
 * ----- createBrokerController -----
 * 1、创建配置类，并利用这些配置类创建 BrokerController
 * ----- BrokerController初始化 BrokerController#initialize -----
 * 2、加载一系列JSON
 *     加载Broker中的主题信息
 *     加载消费进度信息
 *     加载订阅信息
 *     加载订消费者过滤信息
 * 3、创建消息存储管理组件 DefaultMessageStore
 *     如果开启了直接内存存储池 + 异步刷盘 + 主节点，就初始化直接内存存储池
 * 4、加载 storePath 下的所有 CommitLog 文件
 *     每个 CommitLog 都创建一个 MappedFile 对象【存入 MappedFileQueue】，将文件零拷贝到默认内存【这里没有用直接内存】
 *     【CommitLog】就是其持久化文件
 * 5、创建netty服务端【监听10911】
 * ----- BrokerController启动 BrokerController#start -----
 * 6、启动消息存储管理组件，其中有 CommitLog 启动，即下面两个步骤
 *     刷盘线程启动：异步刷盘线程启动之后，每隔 500ms 刷盘一次，MessageStoreConfig.flushIntervalCommitLog=500
 *     Commit线程启动：线程启动之后，每隔 200ms 执行一次提交，默认内存的话不操作，直接内存的话就写入 FileChannel
 * 7、启动netty服务端
 * 8、每隔30s向所有 NameServer 发送心跳包
 */
public static void main(String[] args) {
    start(createBrokerController(args));
}
```

## 存储文件设计

RocketMQ 主要存储的文件包括 CommitLog、ConsumeQueue、 IndexFile、JSON

### CommitLog

==落盘文件如下==
![image-20230430142010915](C:\backup\assets\image-20230430142010915.png)

所有主题的消息都存在 CommitLog 中，生产消息发送时==顺序写==文件，尽最大的能力确保消息发送的高性能与高吞吐量
==但顺序写使读数据变得困难==

文件默认大小为1G，可通过配置文件中的 mapedFileSizeCommitLog 属性来设置大小
MMAP 技术在地址映射的过程中对文件的大小是有限制的，在1.5G-2G之间，所以 CommitLog 文件大小控制在1GB

### ConsumeQueue

==落盘文件如下==
![image-20230430142039189](C:\backup\assets\image-20230430142039189.png)
![image-20230430142057357](C:\backup\assets\image-20230430142057357.png)

为了提升消息消费效率（我得知道msg内容，因此得查 CommitLog 文件），引入了 ConsumeQueue 作为 CommitLog 的索引
每个主题包含多个消息消费队列，每个消息队列对应一个 ConsumeQueue

当 CommitLog（内存）写成功后，ReputMessageService 线程异步定时构建 ConsunmeQueue 和 Index（内存）

根据主题和消费队列ID，得到这个 ConsunmeQueue
根据消费队列的消息序号，计算出索引的位置（比如序号2，就知道偏移量是20），然后直接读取这条索引，再根据索引中记录的消息的全局位置，找到消息

![image-20230501020344180](C:\backup\assets\image-20230501020344180.png)

### IndexFile

==数据结构==

![image-20230501024954536](C:\backup\assets\image-20230501024954536.png)

==落盘文件==

![image-20230430142114989](C:\backup\assets\image-20230430142114989.png)

==IndexFile 的存储内容是什么==
hash槽存储的是索引位置
index条目存储的是 key 的 hash 值，物理的 offset，与 beginTimeStamp 的差值、上一个 hash 冲突的索引位置

==怎么把一条消息放入到 IndexFile==

1. 确定hash槽
   - 根据 key 计算 hashcode，再对 500w 取模，就可以知道位于哪个 hash 槽=N
   - indexHead 占了文件的前面的40字节，每个 hash 槽占4字节，具体在文件的位置是 40 + N * 4
2. index条目首先要跨过indexHead和500w个hash槽的大小。然后根据当前是第几条 index 条目，就放入到第几个位置去
   - 40个字节的 indexHead + 500w * 4字节的hash槽大小 + 索引位置 * 20字节

==怎么查询 IndexFile==
key 根据==主题和消息的 key==计算出，先是根据key计算hashcode，对500w取模，就可以知道位于哪个hash槽。根据槽值的内容，再通过计算index条目位置，获取到index条目，再依次获取上一个hash冲突节点的index条目

### ConfigJson

config 文件夹中存储着 Topic 和 Consumer 等相关信息

- topics.json：主题配置信息
- subscriptionGroup.json：消费者组配置信息
- delayOffset.json：延时消息队列拉取进度
- consumerOffset.json：集群消费模式消息消费进度
- consumerFilter.json：主题消息过滤信息

![image-20230430142132380](C:\backup\assets\image-20230430142132380.png)

### CommitLog、ConsumeQueue的一致性

为什么保证一致性，CommitLog 存储了 ConsumeQueues，即使 ConsumeQueue 丢失也可以通过 CommitLog 恢复出来

## 消息写入流程

Broker 收到消息写入的请求就会进入 SendMessageProcessor 类中 processRequest 方法，最终写入 CommitLog

## 源码分析亮点：MMAP零拷贝提升文件读写性能

传统的 IO 比如从磁盘上读文件，先要把数据读取到内核 IO 缓冲区，然后再从内核 IO 缓冲区中读取到用户空间

MMAP 建立的是磁盘空间到应用程序用户空间的直接映射，数据的传输是一次拷贝过程，不再需要中间的内核 IO 缓冲区

## 源码分析亮点：直接内存

> 直接内存开启的条件：开启直接内存存储池 + 异步刷盘 + 主节点

读操作使用了零拷贝+堆内存
写操作可以使用零拷贝+堆内存，也可以使用直接内存

读写消息如果都走零拷贝+堆内存，并发读写同一块内存不可避免存在锁的问题，造成读写性能下降
如果写操作使用直接内存，就可以实现读写分离，提升读写性能

所以使用直接内存的方式相对来说会比较好，但是肯定的是，需要消耗一定的内存，如果服务器内存吃紧就不推荐这种模式

# Producer

## 消息发送代码示例

```java
public class SyncProducer {
    public static void main(String[] args) throws Exception{
        DefaultMQProducer producer = new DefaultMQProducer("group_test");
        producer.setNamesrvAddr("127.0.0.1:9876");
//        producer.setUnitName("x");
        producer.start();
        for (int i = 0; i < 100; i++) {
            Message msg = new Message("TopicTest", "Tag", ("Hello " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            //发送消息到一个Broker
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n", sendResult);
        }
        producer.shutdown();
    }
}
```

## 生产者启动流程

==核心代码：DefaultMQProducerImpl#start(boolean)==

```java
/**
 * 1、检查生产者组是否满足要求 1不能为空 2不能和系统的分组名冲突
 * 2、获得MQ客户端实例 MQClientInstance
 *     同一个 clientId 只会创建一个
 *     clientId=IP@instanceName@unitName
 * 3、注册当前生产者到到MQ客户端实例中
 * 4、启动MQ客户端实例
 * 5、启动 netty 客户端
 * 6、启动定时任务
 *     2分钟一次获取路由地址
 *     30s一次修改路由信息
 *     30s一次给 Broker 发送心跳
 */
public void start(final boolean startFactory) throws MQClientException {}
```

## 消息发送流程

==核心代码：DefaultMQProducerImpl#sendDefaultImpl==

```java
/**
 * 发送消息核心方法
 * 1、校验消息
 * 2、查找路由
 * 3、选择队列
 *     如果开启了 Broker 故障延迟机制，即 sendLatencyFaultEnable=true 默认不开启
 *         轮询消息队列列表
 *             如果 Broker 可用就用这个队列【Broker可不可用是根据上次的发送时长计算的】
 *             如果 Broker 都不可用，随机选择一个 Broker 随机选择该 Broker 下一个队列进行发送
 *     如果没有开启，就使用默认的选择机制
 *         第一次选择队列，轮询选择
 *         重试的情况下，规避上一次选择的Broker，如果选不出来一个合适的就轮询选择
 *     选择哪个策略？
 *         如果是网络比较好的环境，推荐默认策略，毕竟网络问题导致的发送失败几率比较小
 *         如果是网络不太好的环境，推荐故障延迟机制，避免不断向宕机的 Broker 发送消息，从而实现消息发送高可用
 *         当然以上成立的条件是一个 Topic 创建在2个Broker以上的的基础上
 * 4、消息发送
 *
 * 【计算 Broker 不可用时长用来规避】
 * 如果消息发送异常，默认发送时长为30000ms 规避时间为600s
 * 如果消息发送正常，则根据实际的消息发送时长来获得规避时长
 * 发送时长{50L, 100L, 550L, 1000L, 2000L, 3000L, 15000L};
 * 故障规避{0L, 0L, 30000L, 60000L, 120000L, 180000L, 600000L};
 *     550ms之内，不可用时长为0
 *     达到550ms，不可用时长为30S
 *     达到1000ms，不可用时长为60S
 *     达到2000ms，不可用时长为120S
 *     达到3000ms，不可用时长为180S
 *     达到15000ms，不可用时长为600S
 *
 * 【重试次数】2次
 *
 * 【客户端建立连接的时机】需要与对端进行数据交互时才建立
 */
private SendResult sendDefaultImpl(){}
```

# Consumer（Push）

## 消费者启动流程

==DefaultMQPushConsumerImpl.java==

```java
/**
 * 消费者启动的核心代码入口
 * 1、检查配置信息，比如消费者组不能为空、消费者组不能和系统的分组名冲突等等
 * 2、如果消费模式为集群模式，还需会为该消费组创建一个重试主题
 * 3、获得MQ客户端实例 MQClientInstance【同生产者】
 * 4、一系列值的 copy：DefaultMQPushConsumer → ReBalanceImpl
 * 5、消费进度存储选择
 *     如果是集群模式，使用远程存储存储在 Broker
 *     如果是广播模式，使用本地存储
 * 6、消费进度加载
 *     集群模式不会加载，因为使用远程的
 *     广播模式需要加载到本地 offsetTable
 * 7、消息消费服务启动
 * 8、注册当前消费者到到MQ客户端实例中
 * 9、启动MQ客户端实例
 * 10、启动 netty 客户端
 * 11、启动定时任务
 *     2分钟一次获取路由地址
 *     30s一次获取/修改路由信息，包括主题和对应的Broker列表
 *     30s一次给 Broker 发送心跳
 *     5s一次持久化消费进度【更新偏移量】
 * 12、启动拉消息服务
 *     死循环，从请求队列中弹出一个拉消息请求进行处理，调用到【Broker】消息存储组件的 DefaultMessageStore#getMessage
 *         根据主题和消费队列ID，得到 ConsumeQueue
 *         根据消息序号【offset】，计算出索引的位置（比如序号2，就知道偏移量是20）并从 CommitLog 中取出消息
 *         再凭借回调函数给监听器发送消息
 * 13、启动负载均衡服务，20s执行一次
 *     平均分配消息队列给每个消费者，默认的分配策略是平均，此外还有机房优先、一致性hash
 *     在定时任务中得到的路由信息基础上，给每个 MessageQueue 设置下一个消费的偏移量，加入拉消息的请求队列中
 *     【集群模式】从 Broker 获得，【广播模式】从本地获得
 *     消费者数量的变动，会重新触发负载均衡
 *     增加消费者可以分摊消费，但如果消费者数量比消息队列的总数还多，多出来的消费者将无法分到消息队列消费消息
 */
public synchronized void start() throws MQClientException {}
```

生产消息：轮询发送到主题的各个消息队列

集群模式消费：消费者组中的各个消费者分摊消费消息，即一个消息队列只会被一个消费者消费
广播模式消费：每个消费者消费所有消息队列

顺序消费和并发消费差不多，不同的是顺序消费使用锁机制来确保一个队列同时只能被一个消费者消费，从而确保消费的顺序性
这里有一个定时任务，每20秒做一次续锁，锁的有效期是60S

上述过程是推模式

![image-20230502191051333](C:\backup\assets\image-20230502191051333.png)

![image-20230502171805054](C:\backup\assets\image-20230502171805054.png)

## 消费卡死（顺序消息）

针对顺序消息，我们感觉上会有卡死的现象，由于顺序消息中需要到 Broker 中加锁，这个锁的过期时间是60s，如果某个消费者挂了，60s才能释放锁，所以在这段时间其他消费者消费不了只能等待锁

另外如果 Broker 层面也挂了，走从节点消费，但是从节点上没有锁，所以顺序消息如果发生了这样的情况，也是会有卡死的现象

# 源码亮点备忘

* 读写锁
* 文件存储设计
* 零拷贝：MMAP
* 线程池
* ConcurrentHashMap
* 写时复制容器
* 负载均衡策略
* 故障延迟机制
* 堆外内存
