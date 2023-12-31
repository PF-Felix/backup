# 使用消息队列有什么优点？有什么缺点？

==优点==
异步（可以提高性能）
解耦
流量削峰

==缺点==
可用性降低（可以搭建高可用MQ集群解决）
增加系统复杂性
一致性问题，不是强一致

# 高可用的RocketMQ

![image-20230503045113980](C:\backup\assets\image-20230503045113980.png)

高可用就必须是主从架构

多主：对于某个主题即使一个主宕机，其他主节点依然有这个主题的消息队列可以提供生产和消费

同步复制能够保证数据有热备份

同步刷盘没必要，影响应能，异步刷盘即可

# 为什么RocketMQ性能高

1. 高效的 NIO 框架 netty
2. 使用了零拷贝技术
3. NameServer 中使用了 ReadWriteLock 读写使用不同的锁，能够并发读写提升效率
4. 直接内存，如果使用零拷贝+直接内存，能够实现读写分离提升性能
5. 大量运用了线程池、异步
6. 文件存储：顺序读写、读写分离

# 让你来设计MQ，怎么设计

1. 高效的 NIO 框架 netty
2. 可伸缩、分布式
3. 存储设计：用磁盘存储，顺序读写、读写分离、零拷贝
4. 高可用：主从架构、故障时主从自动切换
5. 消息丢失：同步刷盘、异步刷盘、同步复制、异步复制
6. 运用线程池、异步

# 消息大量堆积怎么处理

临时扩容：增加消息队列、增加并发的消费者数量

# 路由注册&发现&删除

1. Broker 每隔 30s 向所有 NameServer 发送心跳包
2. 生产者和消费者
   1. 2分钟一次获取路由地址
   2. 30s一次获得、修改路由信息
   3. 30s一次给 Broker 发送心跳
3. NameServer 每隔 10s 遍历一次活跃的 Broker 列表，如果某个 Broker 上次上报心跳的时间距离当前时间超过120s 就认为它失效了，关闭连接 + 剔除Broker + 更新路由信息

# 使用过程中遇到过什么问题

并发消费曾经尝试用过增加消费者的方法解决消费速度跟不上生产速度的问题，但是增加了之后发现这个消费者收不到消息，后来查阅了资料之后才知道原因，因为消费者数量超过了消息队列的数量，再后来看了源码中的消息队列分配算法之后更加明白

# 事务消息

![image-20230503090835486](C:\backup\assets\image-20230503090835486.png)