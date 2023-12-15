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

![image-20230405150552642](C:\backup\assets\image-20230405150552642.png)

内存屏障会限制重排序：

> 限制 volatile 变量之间的重排序
> 限制 volatile 变量与普通变量之间的重排序

![image-20230405150813921](C:\backup\assets\image-20230405150813921.png)

![image-20230405150822366](C:\backup\assets\image-20230405150822366.png)

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

![image-20230423024255003](C:\backup\assets\image-20230423024255003.png)

![image-20230423022632611](C:\backup\assets\image-20230423022632611.png)

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

![image-20230421155241364](C:\backup\assets\image-20230421155241364.png)

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

![image-20230421161335533](C:\backup\assets\image-20230421161335533.png)

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

# 线程池

## 阻塞队列







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
