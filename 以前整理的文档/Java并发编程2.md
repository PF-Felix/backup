# volatile与锁

> 标签：并发编程的三大特性

并发编程中的三个问题：原子性问题，可见性问题，有序性问题

volatile可以【保证可见性】和【禁止指令重排】

==原子性==
即一个操作或多个操作，要么全部执行，要么全部不执行
经典例子1：银行转账问题，从账户A减去1000元，往账户B加上1000元
经典例子2：i++
JMM只保证了基本读取和赋值的原子性，更大范围操作的`原子性可以通过锁保证，volatile无法保证原子性`

==可见性==
多个线程访问同一个变量时，一个线程修改了这个变量的值，其他线程能够立即看得到修改的值
`volatile关键字可以保证可见性；锁也能`，但是 volatile 更加轻量，不用切换线程上下文
volatile 利用内存屏障解决可见性问题，而且内存屏障能禁止重排序，`volatile一定程度上能保证有序性`
PS：硬件层面已经实现了《标签：缓存一致性协议》，这里用内存屏障是为了解决 StoreBuffer 的问题

==有序性==
即程序执行的顺序按照代码的先后顺序执行
JMM在单线程中通过单线程语义保证有序性，多线程中保证 happens-before 原则范围内的有序性
`锁能保证有序性`

## volatile VS synchronized

| 对比点     | volatile                                        | synchronized                                                |
| ---------- | ----------------------------------------------- | ----------------------------------------------------------- |
| 原子性     | 无法保证                                        | 可以保证                                                    |
| 可见性     | 可以保证                                        | 可以保证                                                    |
| 有序性     | 一定程度保证                                    | 完全保证                                                    |
| 线程阻塞   | 不会出现线程阻塞                                | 获得锁的线程将阻塞其他线程                                  |
| 适用范围   | 仅能用在变量上                                  | 能修饰方法和代码块                                          |
| 编译器优化 | volatile 变量不会被编译器优化，可以禁止指令重排 | synchronized 变量可以被编译器优化（锁粗化、锁消除、锁升级） |

### volatile的内存语义

> volatile 的读内存语义：当读一个 volatile 变量时，JMM 将该线程对应的本地内存置为无效，从主内存中读取变量
> volatile 的写内存语义：当写一个 volatile 变量时，JMM 将该线程对应的本地内存中的共享变量值刷新到主内存

实现原理是使用内存屏障

> 在每个 volatile 写操作前插入 StoreStore 屏障，后插入 StoreLoad 屏障
> 在每个 volatile 读操作后插入 LoadLoad 屏障，后插入 LoadStore 屏障

PS：这里的内存屏障是 JVM 层面的，并非操作系统层面的，但是本质上还是使用操作系统的内存屏障

> 写内存屏障：促使处理器将当前 StoreBuffer 的值写回主存
> 读内存屏障：促使处理器处理 InvalidateQueue，避免 StoreBuffer 和 InvalidateQueue 的非实时性带来的问题

![image-20230405150552642](D:\markdown-image\image-20230405150552642.png)

内存屏障会限制重排序：

> 限制 volatile 变量之间的重排序
> 限制 volatile 变量与普通变量之间的重排序

![image-20230405150813921](D:\markdown-image\image-20230405150813921.png)

![image-20230405150822366](D:\markdown-image\image-20230405150822366.png)

### 锁的内存语义

> 线程释放锁前，JMM 将共享变量的最新值刷新到主内存中
> 线程获取锁时，JMM 将线程对应的本地内存置为无效，需要共享变量的时候必须去主内存中读取，同时保存在本地内存
> 可以看出，锁释放和 volatile 写具有相同的内存语义；锁获取和 volatile 读具有相同的内存语义

==实现原理==

内置锁（synchronized）

> 同步块：编译器会在同步块的入口位置和退出位置分别插入 monitorenter 和 monitorexit 字节码指令
> 同步方法：编译器会在 Class 文件的方法表中将该方法的 access_flags 字段中的 synchronized 标志位置 1，表示该方法是同步方法并使用调用该方法的对象

显式锁-以 ReentrantLock 公平锁为例

> 加锁：首先会调用 getState() 方法读 volatile 变量 state
> 解锁：setState(int newState) 方法写 volatile 变量 state

显式锁-以 ReentrantLock 非公平锁为例

> 加锁：首先会使用 CAS 更新 volatile 变量 state，更新不成功再去采用公平锁的方式（CAS保证了原子性）
> 解锁：setState(int newState) 方法写 volatile 变量 state

## synchronized VS ReentrantLock

一个是类，一个是关键字

ReentrantLock 功能更加丰富，支持公平锁和非公平锁，可以指定等待锁资源的时间

ReentrantLock 的锁基于 AQS，利用一个 CAS 维护的 volatile 变量实现
synchronized 是基于 ObjectMonitor

## synchronized的优化

==锁消除==synchronized修饰的代码中，如果不存在操作临界资源的情况就消除这个锁

==锁膨胀==如果在一个循环中频繁获取和释放锁，这样带来的消耗很大，锁膨胀就是将锁的范围扩大，避免上下文频繁切换

==锁升级==无锁 → 偏向锁 → 轻量级锁 → 重量级锁，尽量避免使用重量级锁带来的开销

> 无锁：第一次有线程访问锁，锁标志位01不变，偏向锁标志位变为1，线程指针指向当前线程（升级为偏向锁）
> 偏向锁：如果存在锁竞争，即访问锁和线程指针指向的线程不是同一个，锁标志位变为00（升级为轻量级锁）
> 轻量级锁：获得轻量级锁的方式是 CAS，如果自旋到了一定次数还拿不到锁，锁标志位变为10（升级为重量级锁）

![image-20230423024255003](D:\markdown-image\image-20230423024255003.png)

![image-20230423022632611](D:\markdown-image\image-20230423022632611.png)

## 锁优化

==减小锁的粒度==

比如 ConcurrentHashMap 只锁数组的某个元素所在链表

==使用读写锁==

比如 ReentrantReadWriteLock

==读写分离==

CopyOnWriteArrayList

## 锁的种类

==公平锁、非公平锁==

公平锁：如果有别的线程在排队等待锁了，那我就老老实实的往后排队
非公平锁：即使有别的线程在排队等待锁，我也要先尝试获得锁一下，拿到就插队成功，拿不到锁再排队

synchronized 是非公平锁
ReentrantLock、ReentrantReadWriteLock 可以实现公平锁和非公平锁

==悲观锁、乐观锁==

悲观锁：拿不到锁就阻塞等待；synchronized、ReentrantLock、ReentrantReadWriteLock 都是悲观锁
乐观锁：拿不到锁不阻塞，可以继续做别的事情；CAS 是乐观锁的一种实现

==可重入锁、不可重入锁==

==互斥锁、共享锁==

## ReentrantLock

### 加锁过程

![image-20230421155241364](D:\markdown-image\image-20230421155241364.png)

```java
//【面试的话说这里的文字，不要按图片说】
//非公平锁：CAS尝试加锁（state从0改为1），如果加锁失败，就调用acquire
//公平锁没：直接acquire
final void lock() {
    if (compareAndSetState(0, 1))
        //获取锁资源成功，会将当前线程设置为“独占锁的拥有者”
        setExclusiveOwnerThread(Thread.currentThread());
    else
        acquire(1);
}

//1、tryAcquire尝试获得独占锁（不考虑中断）
//     如果state=0，非公平锁：CAS尝试获得锁返回结果，公平锁：没有线程排队才会CAS
//     如果是重入锁，state++
//2、如果没有获得锁，addWaiter(Node.EXCLUSIVE)，将当前线程封装为Node节点，插入到AQS的双向链表的结尾
//3、acquireQueued：加入双向链表之后死循环遍历
//     如果我是第一个排队的节点就tryAcquire，成功返回，不成功继续往下执行
//     如果不是第一个排队的节点，继续往下执行
//         如果node的前置节点是SIGNAL状态，就【阻塞当前线程】
//         如果node的前置节点是CANCELLED状态，将这个CANCELLED的节点从链表移除，继续循环
//         如果node的前置节点是其他状态，就会CAS将其改变为SIGNAL，继续循环
//     总之最终要么是加锁成功，要么是线程阻塞
public final void acquire(int arg) {
    if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}

//【如果是面试到这里就不用往下看了】
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}

//不是第一个排队节点 或 获得锁失败，执行这个方法
//如果node的前置节点是SIGNAL状态返回true，表示node节点的线程需要阻塞
//如果node的前置节点不是SIGNAL，就会CAS将其改变为SIGNAL，下一次循环还是会阻塞
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        return true;
    if (ws > 0) {
        //如果node的前置节点状态是CANCELLED，将这个CANCELLED的节点从链表移除
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        //将node的前置节点的状态改变为SIGNAL
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}

//阻塞当前线程，可以被其他线程的unPark唤醒，或者其他线程调用本线程的中断方法中断阻塞
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

### 解锁过程

![image-20230421161335533](D:\markdown-image\image-20230421161335533.png)

```java
//state--
//如果state==0【true】表示解锁成功，唤醒阻塞的线程（头节点的后继节点中的线程），如果后继节点是null或取消状态，就从后往前遍历找到距离head最近的有效节点
//如果state!=0，啥也不用干
//【面试的话照着图片说就行了，不用说这里的文字】
//【面试的话就不用再往下看了】
public void unlock() {
    sync.release(1);
}

public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}

//唤醒头节点的后继节点Node
//Node被唤醒之后获得锁，被设置为新的头节点
private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    //没啥用
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    //唤醒头节点的下一个节点
    if (s != null)
        LockSupport.unpark(s.thread);
}
```

### Condition

lock.newCondition() 得到的对象，提供了 await 和 signal 方法实现了类似 wait 和 notify 的功能
想执行 await、signal 就必须先持有 lock 锁

await：
1、将当前线程封装成 Node 添加到 Condition 单向链表中
2、当前线程释放锁 Node 脱离 AQS 双向链表

signal：
1、脱离 Condition 单向链表
2、Node 加入 AQS 双向链表

## ReentrantReadWriteLock

比 ReentrantLock 效率高
读读之间不互斥，可以读和读操作并发执行
涉及到了写操作就是互斥的

基于 AQS 实现，对 state 进行操作，读锁基于 state 的高16位进行操作，写锁基于 state 的低16位进行操作

# 并发集合

## ConcurrentHashMap

存储结构：数组+链表+红黑树

通过 CAS 和 synchronized 互斥锁实现的==线程安全==
CAS：在没有hash冲突时（Node要放在数组上时）
synchronized：在出现hash冲突时（Node存放的位置已经有数据了）

### HashMap线程不安全的原因

在 Java7 中，并发执行扩容操作时会造成环形链表和数据丢失的情况
在 Java8 中，并发执行put操作时会发生数据覆盖的情况

### HashMap VS HashTable VS ConcurrentHashMap

| 对比点           | HashMap    | HashTable                                        | ConcurrentHashMap |
| ---------------- | ---------- | ------------------------------------------------ | ----------------- |
| 线程安全性       | 非线程安全 | 线程安全                                         | 线程安全          |
| 是否允许KV为null | 允许       | 不允许                                           | 不允许            |
| 其他             |            | HashTable锁粒度太粗，没有ConcurrentHashMap性能好 | 建议替代HashTable |

### HashMap VS ConcurrentHashMap

> 先参考上个章节

存储结构一样，都是数组+链表+红黑树

链表转换为红黑树的时机一样，都是链表长度>=8
红黑树转换为链表的时机一样，都是树上节点数量<=6

扩容都是变为原来的两倍

扩容时机不一样：

- ConcurrentHashMap：链表长度>=8且数组长度<64 或 当前元素个数大于等于扩容阈值即数组长度（满了再扩容）
- HashMap：元素个数超过负载因子与容量的乘积，负载因子用来衡量何时自动扩容
  例如负载因子默认为 0.75（意味着元素数量达到当前容量的的 3/4 时扩容）

### TreeMap

支持根据 key 自定义排序（默认升序），key 必须实现 Comparable 接口或者在构造方法传入自定义的 Comparator

### put

返回值是 put 之前 key 的 value
put 会覆盖原值，putIfAbsent 不会

```java
public V put(K key, V value) {
    return putVal(key, value, false);
}

public V putIfAbsent(K key, V value) {
    return putVal(key, value, true);
}
```

#### 添加数据到数组

```java
final V putVal(K key, V value, boolean onlyIfAbsent) {
    //不允许key或者value出现null（HashMap 支持 null value 与 null key）
    if (key == null || value == null) throw new NullPointerException();

    //(h ^ (h >>> 16)) & HASH_BITS; HashMap中没有HASH_BITS
    //h ^ (h >>> 16) 是扰动计算，目的是尽量使元素分布均匀减少 hash 碰撞，下面举例说明
    //    假设数组的初始化容量为16即10000，length-1=15即1111
    //    假设几个对象的 hashCode 为 1100 10010、1110 10010、11101 10010，不做扰动计算将发生 hash 碰撞（取模值相等）
    //HASH_BITS = ‭01111111111111111111111111111111‬
    //HASH_BITS 让 hash 的最高位肯定为0代表正数（其他位不变），因为 hash 值为负数时有特殊的含义
    //static final int MOVED     = -1; //代表当前位置正在扩容
    //static final int TREEBIN   = -2; //代表当前位置是一棵红黑树
    //static final int RESERVED  = -3; // hash for transient reservations
    int hash = spread(key.hashCode());

    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        //n：数组长度
        //i：索引位置
        //f：i索引位置的Node对象
        //fh：i索引位置上数据的hash值
        Node<K,V> f; int n, i, fh;

        //需要的话先初始化数组
        if (tab == null || (n = tab.length) == 0)
            tab = initTable();

        //（取模运算）定位元素在数组哪个索引：hash & (length -1)，运算结果最大值为 length -1，不会出现数组下标越界的情况
        //如果这个位置没有数据，就把数据放在这个位置（CAS）
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value, null)))
                break;
        }

        //如果索引位置有数据，且正在扩容，就协助扩容
        else if ((fh = f.hash) == MOVED)
            tab = helpTransfer(tab, f);

        //索引位置有数据，且没有在扩容，把元素插入链表或红黑树，加锁，互斥锁锁住一个索引，其他索引可以正常访问
        //然后如果链表长度>=8，转化为红黑树或扩容
        else {
            V oldVal = null;
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    //（元素的hash>=0）遍历链表添加元素
                    if (fh >= 0) {
                        binCount = 1; //...遍历期间binCount++
                    }

                    //如果fh<0（元素的hash<0），且索引位置是红黑树，向树中插入元素
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key, value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) {
                //链表长度>=8，转为红黑树或扩容
                //为何是8？根据泊松分布，链表长度到8的概率非常低，源码中是0.00...6，尽量在避免生成红黑树使写入成本过高
                //            数组长度<64，扩容，容量翻倍
                //            数组长度>=64，转为红黑树
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }

    //数量+1 且判断是否需要扩容
    addCount(1L, binCount);
    return null;
}
```

#### 初始化

```java
//sizeCtl：是数组在初始化和扩容操作时的一个控制变量
//0：代表数组还没初始化
//大于0：代表当前数组的扩容阈值，或者是当前数组的初始化大小
//-1：代表当前数组正在初始化
//小于-1：低16位代表当前数组正在扩容的线程个数（如果1个线程扩容，值为-2，如果2个线程扩容，值为-3）
private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    while ((tab = table) == null || tab.length == 0) {
        if ((sc = sizeCtl) < 0)
            Thread.yield();

        //CAS将SIZECTL改为-1，尝试初始化
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            try {
                if ((tab = table) == null || tab.length == 0) {
                    //如果sizeCtl > 0 就初始化sizeCtl长度的数组；如果sizeCtl == 0，就初始化默认的长度16
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    table = tab = nt;
                    //将sc赋值为下次扩容的阈值
                    sc = n - (n >>> 2);
                }
            } finally {
                //sizeCtl为下次扩容的阈值
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}
```

#### treeifyBin

```java
private final void treeifyBin(Node<K,V>[] tab, int index) {
    Node<K,V> b; int n, sc;
    if (tab != null) {
        if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
            tryPresize(n << 1);

        //转红黑树需要加锁
        else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
            synchronized (b) {
                //省略一大段代码
            }
        }
    }
}

private final void tryPresize(int size) {
    int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY :
    tableSizeFor(size + (size >>> 1) + 1);
    int sc;

    //数组没有在初始化，也没有在扩容
    while ((sc = sizeCtl) >= 0) {
        Node<K,V>[] tab = table; int n;

        //初始化，同上
        if (tab == null || (n = tab.length) == 0) {
            //省略
        }

        //容量已经到了最大，就不扩容了
        else if (c <= sc || n >= MAXIMUM_CAPACITY)
            break;

        //扩容，帮助其他线程扩容 或 自己扩容
        else if (tab == table) {
            int rs = resizeStamp(n);
            if (sc < 0) {
                Node<K,V>[] nt;
                //判断是否可以协助扩容
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                    transferIndex <= 0)
                    break;
                //sizeCtl+1，表示扩容的线程数量+1，并协助扩容
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }

            //自己扩容，sizeCtl+2，表示当前有一个线程在扩容
            else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2))
                transfer(tab, null);
        }
    }
}
```

#### addCount

```java
private final void addCount(long x, int check) {
    CounterCell[] as; long b, s;
    //不重要
    if ((as = counterCells) != null ||
        !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
        //省略一大段代码
    }

    if (check >= 0) {
        Node<K,V>[] tab, nt; int n, sc;
        //当前元素个数大于等于扩容阈值，且数组不为null，且数组长度没有达到最大值，协助其他线程扩容或自己扩容
        while (s >= (long)(sc = sizeCtl) && (tab = table) != null && (n = tab.length) < MAXIMUM_CAPACITY) {
            int rs = resizeStamp(n);
            //省略一段和 tryPresize 一样的代码
            s = sumCount();
        }
    }
}
```

#### 扩容

支持多线程并发扩容

触发扩容的三个点：

- 链表转红黑树前，会判断是否需要扩容
- addCount 方法中，如果元素数量超过阈值，触发扩容
- putAll 方法中，根据传入的 map.size 判断是否需要扩容

```java
//tab：老数组
//nextTab：新数组
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
    int n = tab.length, stride;

    //基于CPU内核数计算每个线程一次性迁移多少数据
    //每个线程迁移数组长度最小值是MIN_TRANSFER_STRIDE=16
    if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
        stride = MIN_TRANSFER_STRIDE;

    //没有新数组的话，创建一个容量翻倍的新数组
    if (nextTab == null) {
        try {
            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
            nextTab = nt;
        } catch (Throwable ex) {
            sizeCtl = Integer.MAX_VALUE;
            return;
        }
        nextTable = nextTab;

        //扩容总进度，>=transferIndex的索引都已分配出去
        transferIndex = n;
    }
    int nextn = nextTab.length;
    ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab); //MOVED节点
    boolean advance = true;
    boolean finishing = false; // to ensure sweep before committing nextTab
    for (int i = 0, bound = 0;;) {
        Node<K,V> f; int fh;
        while (advance) {
            int nextIndex, nextBound;

            //第一次循环不会进来
            //之后领取了任务之后就可以进来了，按索引顺序从后往前一个个处理
            if (--i >= bound || finishing)
                advance = false;

            //transferIndex <=0 表示所有索引都迁移完成
            else if ((nextIndex = transferIndex) <= 0) {
                i = -1;
                advance = false;
            }

            //当前线程尝试领取任务，领取一段数组的数据迁移
            else if (U.compareAndSwapInt(this, TRANSFERINDEX, nextIndex,
                      nextBound = (nextIndex > stride ? nextIndex - stride : 0))) {
                bound = nextBound;
                //标记索引位置
                i = nextIndex - 1;
                advance = false;
            }
        }

        //i < 0，即线程没有接收到任务，扩容线程数量-1，结束扩容操作
        if (i < 0 || i >= n || i + n >= nextn) {
            int sc;
            if (finishing) {
                nextTable = null;
                table = nextTab;
                sizeCtl = (n << 1) - (n >>> 1);
                return;
            }
            if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                    return;
                finishing = advance = true;
                i = n; // recheck before commit
            }
        }

        //如果当前索引位置没数据，无需迁移，标记为MOVED
        else if ((f = tabAt(tab, i)) == null)
            advance = casTabAt(tab, i, null, fwd);

        //如果当前索引位置的 hash 是 MOVED，表示已经迁移过了
        else if ((fh = f.hash) == MOVED)
            advance = true;

        //迁移数据，将 oldTable 的数据迁移到 newTable，加锁
        else {
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    Node<K,V> ln, hn;

                    //正常情况，链表
                    if (fh >= 0) {
                        //运算结果是0或n：比如16（即10000）&任何hash 只有10000（hash=X1XXXX）和0（hash=X0XXXX）两种结果
                        //hash&15（即01111）的结果是A
                        //hash&31（即11111）的结果是B
                        //A和B只有两种情况：A==B（hash=X0XXXX） 或者 A+16==B（hash=X1XXXX）
                        //因此扩容后索引位置不变 或 索引位置+n
                        int runBit = fh & n;
                        Node<K,V> lastRun = f;

                        //找出最后一段 hash&n 连续不变的链表
                        //    即从 lastRun 开始后面的这一段数据就不用重新创建节点了，前面的数据还需要创建节点
                        //runBit == 0 表示扩容前后索引位置不变，其他情况表示索引位置需要变动
                        for (Node<K,V> p = f.next; p != null; p = p.next) {
                            //这里也是0或n
                            int b = p.hash & n;
                            if (b != runBit) {
                                runBit = b;
                                lastRun = p;
                            }
                        }
                        if (runBit == 0) {
                            ln = lastRun;
                            hn = null;
                        }
                        else {
                            hn = lastRun;
                            ln = null;
                        }

                        //lastRun 之前的结点重新创建节点
                        for (Node<K,V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash; K pk = p.key; V pv = p.val;
                            if ((ph & n) == 0)
                                ln = new Node<K,V>(ph, pk, pv, ln);
                            else
                                hn = new Node<K,V>(ph, pk, pv, hn);
                        }
                        //低位链表
                        setTabAt(nextTab, i, ln);
                        //高位链表
                        setTabAt(nextTab, i + n, hn);
                        //设置当前索引为MOVED
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }

                    //红黑树
                    else if (f instanceof TreeBin) {
                        //忽略一大段代码
                    }
                }
            }
        }
    }
}
```

## CopyOnWriteArrayList

是线程安全的 ArrayList

写数据时加锁，同时复制一个数组的副本，写这个副本，完成写之后再用副本替换掉原数组；写操作不影响读操作

初始长度是0

使用迭代器做写操作会抛异常

# 并发工具类

## CountDownLatch

核心实现就是一个计数器

模拟有三个任务需要并行处理，在三个任务全部处理完毕后，再执行后续操作

执行 await 方法，判断 state 是否为0，如果为0直接执行后续任务，如果不为0插入到AQS的双向链表并挂起线程

执行 countDown 方法，代表一个任务结束，计数器减1，如果计数器变为0就唤醒阻塞的线程

![image-20230423013820926](D:\markdown-image\image-20230423013820926.png)

PS：CountDownLatch 的工作用 join 也可以做到

```java
public class JoinTest {
    public static void main(String[] args) throws InterruptedException {
        Thread parser1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("部件一生产完成");
            }
        });

        Thread parser2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("部件二生产完成");
            }
        });

        parser1.start();
        parser2.start();
        parser1.join();
        parser2.join();
        System.out.println("机器人组装完毕");
    }
}

public class CountDownLatchTest {
    static CountDownLatch c = new CountDownLatch(2);
    public static void main(String[] args) throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("部件1生产完成");
                c.countDown();
                System.out.println("部件2生产完成");
                c.countDown();
            }
        }).start();

        c.await();
        System.out.println("机器人组装完成");
    }
}
```

输出：

```
部件一生产完成
部件二生产完成
机器人组装完毕
```

join() 将调用者合并入当前线程，当前线程等待 join 线程执行结束

## CyclicBarrier

原理：多个线程等待，直到线程数量达到临界点再继续执行

1. 构造函数中传入的参数为线程数量
1. 调用 await 方法的线程进入阻塞状态
1. 最后一个线程调用 await 方法会唤醒其他所有阻塞线程

PS：CountDownLatch 的计数器只能设置一次，而 CyclicBarrier 的计数器可以使用 reset() 方法重置

## Semaphore

通常使用 Semaphore 限制同时并发的线程数量

线程执行操作时先通过 acquire 方法获得许可（计数器不为0，计数器减1），执行完毕再通过 release 方法释放许可（计数器加1）
如果无可用许可（计数器为0），acquire 方法将一直阻塞，直到其它线程释放许可

很像线程池，但线程池的线程数量是一定的可复用，Semaphore 并没有复用


# 线程池

## 阻塞队列

### 入队出队方法

看 BlockingQueue 接口

入队方法：

```java
add(E)     	//入队，如果队列满了，无法存储，抛出异常；对于有长度限制的队列，建议使用 offer
offer(E)    //入队，如果队列满了，返回false
offer(E,timeout,unit)   //入队，队列满的情况下阻塞等待，如果阻塞一段时间，依然没添加进入，返回false
put(E)      //入队，如果队列满了，挂起线程，等到队列中有位置，再扔数据进去，死等！
```

出队方法：

```java
remove()    //出队，如果队列为空，抛出异常
poll()      //出队，如果队列为空，返回null
peek()      //取队首不出队，如果队列为空，返回null
poll(timeout,unit)   //出队，队列为空的情况下阻塞等待一段时间，如果期间队列有数据就出队成功，反之返回null
take()      //出队，如果队列为空，阻塞死等
```

### ArrayBlockingQueue

有界队列，是基于数组实现的，创建时必须指定数组长度，创建之后无法更改容量

成员变量：

```java
final Object[] items; //数组
int takeIndex; //数组下标，下次出队时使用
int putIndex; //数组下标，下次入队时使用
int count; //队列的元素个数
final ReentrantLock lock; //主锁，保护所有访问
private final Condition notEmpty; //用于挂起和唤醒消费者线程，出队时可能阻塞消费者线程，每次入队成功都会唤醒消费者线程
private final Condition notFull; //用于挂起和唤醒生产者线程，入队时可能阻塞生产者线程，每次出队成功都会唤醒生产者线程
```

### LinkedBlockingQueue

有界/无界队列，基于链表实现，如果不指定链表容量，默认容量为`Integer.MAX_VALUE`

成员变量：

```java
private final int capacity;  //链表容量
private final AtomicInteger count = new AtomicInteger(); //队列元素个数
transient Node<E> head;  //头指针，取
private transient Node<E> last;  //尾指针，存
private final ReentrantLock takeLock = new ReentrantLock();  //消费者的锁
private final ReentrantLock putLock = new ReentrantLock();  //生产者的锁

//用于挂起和唤醒生产者线程，入队时可能阻塞生产者线程，每次出队成功都会唤醒生产者线程
private final Condition notFull = putLock.newCondition();
//用于挂起和唤醒消费者线程，出队时可能阻塞消费者线程，每次入队成功都会唤醒消费者线程
private final Condition notEmpty = takeLock.newCondition();
//因为生产者和消费者使用的是不同的锁不互斥，入队的同时可能也有出队，因此入队时也会判断是否需要唤醒生产者线程（反之亦然）
```

### PriorityBlockingQueue

无界队列，基于数组实现（会扩容），排序基于二叉堆算法，不允许元素 null
是排序的（不是先进先出），自定义对象通过 Comparable 接口来比较顺序

成员变量：

```java
private static final int DEFAULT_INITIAL_CAPACITY = 11;  //数组的初始长度
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;  //数组的最大长度
private transient Object[] queue;  //底层是基于数组实现的二叉堆
private transient int size;  //队列中元素个数
private transient Comparator<? super E> comparator;
private final ReentrantLock lock;  //所有操作的锁

//用于挂起和唤醒消费者线程；无界队列不需要挂起和唤醒生产者线程因此没有关于生产者的Condition
private final Condition notEmpty;
```

入队可能伴随扩容，入队和出队伴随建立二叉堆

### DelayQueue

无界队列，排序基于二叉堆算法
是排序的（不是先进先出），排序规则自定义，实现 Comparable 接口

```java
@Data
public class DelayQueueTest implements Delayed {

    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 任务执行时间
     */
    private long currentMillSeconds;

    public static void main(String[] args) throws InterruptedException {
        DelayQueue<DelayQueueTest> queue = new DelayQueue<>();
        queue.add(new DelayQueueTest("任务1", 5000L));
        queue.add(new DelayQueueTest("任务2", 3000L));
        queue.add(new DelayQueueTest("任务3", 8000L));

        System.out.println(queue.take().getTaskName());
        System.out.println(queue.take().getTaskName());
        System.out.println(queue.take().getTaskName());
    }

    /**
     * @param taskName 任务名称
     * @param delayMillSeconds 延迟时间
     */
    public DelayQueueTest(String taskName, long delayMillSeconds) {
        this.taskName = taskName;
        this.currentMillSeconds = System.currentTimeMillis() + delayMillSeconds;
    }

    @Override
    public int compareTo(Delayed o) {
        return new Long((currentMillSeconds - ((DelayQueueTest)o).currentMillSeconds)).intValue();
    }

    /**
     * 设置延迟时间，每时每刻都在动态变化
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return currentMillSeconds - System.currentTimeMillis();
    }
}
```

### SynchronousQueue

这个队列不存储数据

生产数据的时候如果有消费者在等待就能互通数据，消费数据的时候如果有生产者在等待也能互通数据

如果是公平，采用Queue，如果是不公平，采用Stack，默认是不公平

## newFixedThreadPool

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads,
                                  nThreads,
                                  0L,
                                  TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>()
                                 );
}
```

线程数固定，使用无界队列

暂时处理不了的线程加入无界队列

队列是无界的，若消费不过来，会导致内存被任务队列占满，最终oom

## newSingleThreadExecutor

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1,
                                1,
                                0L,
                                TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>())
        );
}
```

线程池中只有一个工作线程在处理任务

暂时处理不了的线程加入无界队列

如果业务涉及到顺序消费，可以采用 newSingleThreadExecutor

无界队列，可以无限的往里面添加任务，直到内存溢出

## newCachedThreadPool

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```

只要有任务提交，就必然有工作线程可以处理

工作线程执行完任务之后，60秒内如果有新任务进来就再次拿到这个任务去执行，如果闲置了60秒无任务执行会结束

创建大量的线程会导致严重的性能问题

## newScheduledThreadPool

```java
//本质上还是ThreadPoolExecutor
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
    return new ScheduledThreadPoolExecutor(corePoolSize);
}

public ScheduledThreadPoolExecutor(int corePoolSize) {
    super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue());
}
```

支持延时执行（基于 DelayQueue 实现）与周期执行（任务完成之后再次进入阻塞队列）

```java
public class ThreadPoolTest {

    @Test
    public void testNewScheduleThreadPool() throws IOException, InterruptedException {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(3);

        //正常执行
        service.execute(() -> {
            System.out.println(Thread.currentThread().getName());
        });

        //延迟5秒执行
        service.schedule(() -> {
            System.out.println(Thread.currentThread().getName());
        },5, TimeUnit.SECONDS);

        Thread.sleep(10000L);

        //周期执行，第一次延迟5s执行，然后每3s执行一次
        //本次任务开始时，间隔period的时间间隔就可以执行下一个任务了
        int period = 1;
//        int period = 4;
        System.out.println(System.currentTimeMillis());
        service.scheduleAtFixedRate(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis());
        },2, period, TimeUnit.SECONDS);

        //周期执行，当前任务第一次延迟5s执行，然后没3s执行一次
        //本次任务结束后，间隔delay的时间才会执行下一个任务
        System.out.println(System.currentTimeMillis());
        service.scheduleWithFixedDelay(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis());
        },2,1, TimeUnit.SECONDS);

        System.in.read();
    }
}
```

## newSingleThreadScheduledExecutor（略）

## newWorkStealingPool

前面的线程池都是基于 ThreadPoolExecutor 实现的

而 newWorkStealingPool 是基于 ForkJoinPool 实现的，可以将一个任务拆分为多个任务多线程并行执行
需要任务大耗时长，拆分并行执行才可能提升效率

```java
public static ExecutorService newWorkStealingPool() {
    return new ForkJoinPool
        (Runtime.getRuntime().availableProcessors(),
         ForkJoinPool.defaultForkJoinWorkerThreadFactory,
         null, true);
}
```

```java
public class ThreadPoolTest {

    @Test
    public void testNewWorkStealingPool() throws IOException {
        int[] nums = new int[500000000];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = (int) ((Math.random()) * 1000);
        }

        System.out.println("单线程计算数组总和");
        long start = System.nanoTime();
        int sum = 0;
        for (int num : nums) {
            sum += num;
        }
        long end = System.nanoTime();
        System.out.println("单线程运算结果为：" + sum + "，计算时间为：" + (end  - start));

        ForkJoinPool forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool();
        System.out.println("分而治之计算数组总和");
        long forkJoinStart = System.nanoTime();
        ForkJoinTask<Integer> task = forkJoinPool.submit(new SumRecursiveTask(nums, 0, nums.length - 1));
        Integer result = task.join();
        long forkJoinEnd = System.nanoTime();
        System.out.println("分而治之运算结果为：" + result + "，计算时间为：" + (forkJoinEnd  - forkJoinStart));
    }
}

@Data
class SumRecursiveTask extends RecursiveTask<Integer>{
    private int[] nums;
    private int start;
    private int end;
    private final int MAX_STRIDE = 50000000;

    public SumRecursiveTask(int[] nums, int start, int end) {
        this.nums = nums;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        int sum = 0;
        int stride = end - start;
        if(stride <= MAX_STRIDE){
            for (int i = start; i <= end; i++) {
                sum += nums[i];
            }
        } else {
            //将任务拆分为两个任务
            int middle = (start + end) / 2;
            SumRecursiveTask left = new SumRecursiveTask(nums, start, middle);
            SumRecursiveTask right = new SumRecursiveTask(nums, middle + 1, end);
            //分别执行
            left.fork();
            right.fork();
            sum = left.join() + right.join();
        }
        return sum;
    }
}
```



## 自定义线程池

### 为什么要自定义

如果直接采用 JDK 提供的方式去构建，可以设置的核心参数最多就两个，这样就会导致对线程池的控制粒度很粗
推荐自定义线程池

### 核心参数

```java
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {
    if (corePoolSize < 0 ||
        maximumPoolSize <= 0 ||
        maximumPoolSize < corePoolSize ||
        keepAliveTime < 0)
        throw new IllegalArgumentException();
    if (workQueue == null || threadFactory == null || handler == null)
        throw new NullPointerException();
    this.acc = System.getSecurityManager() == null ?
        null :
    AccessController.getContext();
    this.corePoolSize = corePoolSize;  //核心线程数量，任务结束之后核心线程不会被销毁，个数是允许为0
    this.maximumPoolSize = maximumPoolSize;  //最大工作线程数量，大于0，大于等于核心线程数
    this.workQueue = workQueue;  //任务在没有核心工作线程处理时，任务先扔到阻塞队列中
    this.keepAliveTime = unit.toNanos(keepAliveTime);  //空闲的非核心工作线程的最大存活时间，可以等于0
    this.threadFactory = threadFactory;  //用于构建线程，可以设置thread的一些信息
    this.handler = handler;  //当线程池无法处理投递过来的任务时，执行的拒绝策略
    //阻塞队列，线程工厂，拒绝策略都不允许为null，为null就扔空指针异常
}
```

当队列和线程池都满了，需要拒绝策略处理任务：

> AbortPolicy：直接抛出一个异常
> CallerRunsPolicy：将任务交给调用者处理（不建议）
> DiscardPolicy：直接将任务丢弃掉
> DiscardOldestPolicy：将队列中最早的任务丢弃掉，将当前任务再次尝试交给线程池处理

execute 无返回结果，submit 有返回结果

如果是局部变量的线程池，用完要 shutdown
如果是全局的线程池，很多业务都会到，使用完毕后不要 shutdown

### 成员变量

> 标签：线程池状态；线程池的状态

```java
//ctl的高3位，表示线程池状态
//ctl的低29位，表示工作线程的个数
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

//常量29
private static final int COUNT_BITS = Integer.SIZE - 3;
//‭00011111 11111111 11111111 11111111‬ 工作线程的最大数量
private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

//111：RUNNING状态，RUNNING可以处理任务，并且处理阻塞队列中的任务
private static final int RUNNING    = -1 << COUNT_BITS;
//000：SHUTDOWN状态，不会接收新任务，正在处理的任务正常进行，阻塞队列的任务也会做完
private static final int SHUTDOWN   =  0 << COUNT_BITS;
//001：STOP状态，不会接收新任务，正在处理任务的线程会被中断，阻塞队列的任务一个不管
private static final int STOP       =  1 << COUNT_BITS;
//010：TIDYING状态，这个状态是由SHUTDOWN或者STOP转换过来的，代表当前线程池马上关闭，就是过渡状态
private static final int TIDYING    =  2 << COUNT_BITS;
//011：TERMINATED状态，这个状态是TIDYING状态转换过来的，转换过来只需要执行一个terminated方法
private static final int TERMINATED =  3 << COUNT_BITS;

//在使用下面这几个方法时，需要传递ctl进来
//基于&运算的特点，保证只会拿到ctl高三位的值
private static int runStateOf(int c)     { return c & ~CAPACITY; }
//基于&运算的特点，保证只会拿到ctl低29位的值
private static int workerCountOf(int c)  { return c & CAPACITY; }
private static int ctlOf(int rs, int wc) { return rs | wc; }
```

状态流转：

![image-20230420031904637](D:\markdown-image\image-20230420031904637.png)

### 核心参数设计规则

1、CPU密集型
一般为CPU核心数+1
因为CPU密集型任务CPU的使用率很高，若开过多的线程，只能增加线程上下文的切换次数，带来额外的开销

2、IO密集型
因为IO的程度不一样，有的是1s，有的是15s，有的是1分钟
需要压测，观察CPU占用情况（70-80足矣），经过多次调整，来决定核心线程数

如果每次修改项目都需要重新部署，成本太高了，可以采用一些开源项目提供的方式去做监控和修改
比如 hippo4j 就可以对线程池进行监控
Github地址：https://github.com/opengoofy/hippo4j
官方文档：https://hippo4j.cn/docs/user_docs/intro

# 异步编程

## FutureTask

FutureTask 是一个异步任务的类，一般配合 Callable 使用，可以取消任务，查看任务是否完成，获取任务的返回结果

状态流转：

```java
/**
 * NEW -> COMPLETING -> NORMAL           任务正常执行，并且返回结果也正常返回
 * NEW -> COMPLETING -> EXCEPTIONAL      任务正常执行，但是结果是异常
 * NEW -> CANCELLED                      任务被取消
 * NEW -> INTERRUPTING -> INTERRUPTED    任务被中断
 */
//记录任务的状态
private volatile int state;
//任务被构建之后的初始状态
private static final int NEW          = 0;
private static final int COMPLETING   = 1;
private static final int NORMAL       = 2;
private static final int EXCEPTIONAL  = 3;
private static final int CANCELLED    = 4;
private static final int INTERRUPTING = 5;
private static final int INTERRUPTED  = 6;
```

## CompletableFuture

```java
/**
 * CompletableFuture
 * 任务编排
 */
public class CompletableFutureTest {

    /**
     * runAsync 无返回值
     * supplyAsync 有返回值
     * 这两个方法都可以指定线程池，如果没有指定的话默认使用 ForkJoinPool
     */
    @Test
    public void testRunAsync() throws Exception {
        CompletableFuture.runAsync(() -> {
            System.out.println("任务1");
        });

        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2");
            return "任务2的结果";
        });
        while (completableFuture.isDone()) {
            System.out.println(completableFuture.get());
            break;
        }
    }

    /**
     * accept 前置任务完成，就触发回调，回调能够得到前置任务的返回值
     * acceptAsync 能够指定线程池，如果不指定就用默认的线程池
     * thenRun thenRunAsync 同上 区别在于这个方法不能接收前置方法的返回值
     * thenApply thenApplyAsync 同上 区别在于这个方法能接收前置方法的返回值，自己也有返回值
     */
    @Test
    public void testAccept() throws Exception {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("任务1");
        }).thenAccept(c -> {
            System.out.println("任务2");
            System.out.println(c);
        }).get();

        System.out.println("=====");

        CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1");
            return "任务1的结果";
        }).thenAccept(c -> {
            System.out.println("任务2");
            System.out.println(c);
        }).get();
    }

    /**
     * acceptEither 任何一个任务完成，就触发回调
     * acceptEitherAsync 能够指定线程池，如果不指定就用默认的线程池
     * runAfterEither applyToEither 是类似的
     *
     * 同理：runAfterBoth thenAcceptBoth thenCombine 同上
     */
    @Test
    public void testAcceptEither() throws Exception {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("任务1");
        }).acceptEither(
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("任务2");
                }),
                (c) -> {
                    System.out.println("任务3");
                    System.out.println(c);
                }
        ).get();

        System.out.println("=====");

        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("任务1");
            return "任务1的结果";
        }).acceptEither(
                CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("任务2");
                    return "任务2的结果";
                }),
                (c) -> {
                    System.out.println("任务3");
                    System.out.println(c);
                }
        ).get();
    }

    /**
     * exceptionally thenCompose handle 可以用于处理异常
     *
     * allOf 的方式是让内部编写多个 CompletableFuture 的任务，多个任务都执行完后，才会继续执行后续拼接的任务；无返回值
     * anyOf 的方式是让内部编写多个 CompletableFuture 的任务，只要有一个前置任务执行完毕就继续执行后续拼接的任务
     */
}
```

# !!!

# 什么是线程上下文切换

不同的线程切换使用CPU就是上下文切换

# 如何保证线程安全性

锁、volatile、CAS、原子类（atomic）、线程安全的类

# CAS

是一种乐观锁，能保证对一个变量的替换是原子性的

缺点：

- 无法保证多个变量的原子性
  解决方案可以是 ReentrantLock 锁（AQS）基于 volatile 和 CAS 实现
  可以使用 AtomicReference 将多个变量放到一个对象中操作
- ABA问题：可以引入版本号来解决，有一个这样的实现 AtomicStampeReference
- 长时间自旋不成功浪费 CPU：控制好自旋的次数

# AQS

## 介绍

AQS 是一个抽象类
ReentrantLock、ThreadPoolExecutor、阻塞队列、CountDownLatch、Semaphore、CyclicBarrier 都是基于AQS实现

AQS 的核心实现：

- 一个 volatile 修饰的 int 类型的 state 变量（基于 CAS 修改），解决并发编程的三大问题：原子性、可见性、有序性
- 一个存储阻塞线程的双向链表，如果一个线程获取不到资源就会封装成一个 Node 对象并放入这个链表中

如果面试问到了这个问题，可以扩展谈谈 ReentrantLock

## 为什么用双向链表

是为了方便操作

线程在排队期间是可以取消的，取消某个节点需要将前继节点的 next 指向后继节点
如果是单向链表，只能找到前继节点或后继节点其中一个，要找到另一个需要遍历整个链表，这样效率低
所以采用双向链表的结构

## 为什么有一个虚拟的head节点

只是为了方便操作，没有虚拟head节点也可以实现 AQS

# ReentrantLock释放锁时为什么要从后往前找有效节点

因为线程排队加入链表不是原子性的（addWaiter 方法）

1. 将当前节点的前置节点指向 tail 代表的节点
2. CAS 将 tail 指向当前节点
3. 将前置节点的后继节点指向当前节点

如果从前往后遍历，此时恰好有个节点排队入链表尾部，很可能遍历的时候把这个节点漏掉（从后往前没有这个问题）

# ConcurrentHashMap

## lastRun机制

## 数组长度为什么是2的n次幂

> 同HashMap

为了数据分布均匀，如果数组长度不是2的n次幂，会破坏咱们散列算法，导致hash冲突增加

还会破坏后面很多的算法，比如lastRun机制

## 如何保证数组初始化线程安全

使用了 DCL（双重检查锁）

锁的实现是基于 CAS 的，初始化数组时，CAS 将 sizeCt 改为负1

外层 while 循环判断数组未初始化，基于 CAS 加锁，然后在内层再次判断数组未初始化

# 线程池

## 为什么添加空任务的非核心线程

```java
addWorker(firstTask:null,core:false);
```

线程池可以设置核心线程数是0个
另外，线程池有一个属性如果设置为`true`核心线程也会被干掉

没有核心线程的情况下，任务进入阻塞队列，可能没有工作线程处理（添加一个空任务的非核心线程可以避免这种情况）

## 工作线程没任务时在干嘛

如果是核心线程，会调用阻塞队列的 take 方法，直到拿到任务为止

如果是非核心线程，会调用阻塞队列的 poll 方法，等待最大空闲时间，如果没任务直接销毁，如果有活就正常干
