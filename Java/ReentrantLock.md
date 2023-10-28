# 加锁

<img src="D:\ImageA\20231008105717.png" style="zoom:33%;" />

<img src="D:\ImageA\20231008105733.png" style="zoom:39%;" />

根据上图，**公平锁和非公平锁**唯一的区别就在这里：
- 非公平锁：无论何时都不会先排队，而是直接尝试 CAS 竞争锁
- 公平锁：队列不空就先排队，队列为空才会尝试 CAS 竞争锁

<img src="D:\ImageA\20231008105752.png" style="zoom:50%;" />

相关的详细代码如下：

<img src="D:\ImageA\20231008105805.png" style="zoom:67%;" />

```java
//如果没有获得锁，addWaiter(Node.EXCLUSIVE)，将当前线程封装为Node节点，插入到AQS的双向链表的结尾
//acquireQueued：加入双向链表之后死循环遍历
//  如果我是第一个排队的节点就tryAcquire，如果加锁失败就继续往下执行
//  如果不是第一个排队的节点，继续往下执行shouldParkAfterFailedAcquire
//      如果前置节点是SIGNAL状态，返回true，接下来【阻塞当前线程】
//      如果前置节点是CANCELLED状态，将这个CANCELLED的节点从链表移除，继续循环
//      如果前置节点是其他状态，就会CAS将其改变为SIGNAL，继续循环
//  总之最终要么是加锁成功，要么是线程阻塞
final boolean acquireQueued(final Node node, int arg) {
    boolean interrupted = false;
    try {
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node))
                interrupted |= parkAndCheckInterrupt();
        }
    } catch (Throwable t) {
        cancelAcquire(node);
        if (interrupted)
            selfInterrupt();
        throw t;
    }
}

private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        return true;
    if (ws > 0) {
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        pred.compareAndSetWaitStatus(ws, Node.SIGNAL);
    }
    return false;
}

private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);
    return Thread.interrupted();
}

public static void park(Object blocker) {
    Thread t = Thread.currentThread();
    setBlocker(t, blocker);
    UNSAFE.park(false, 0L);
    setBlocker(t, null);
}
```

# 解锁

<img src="D:\ImageA\20231008105824.png" style="zoom:40%;" />

<img src="D:\ImageA\20231008105841.png" style="zoom:56%;" />

# 标签

标签：`#锁`