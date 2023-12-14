LSM (Log-Structured Merge Tree) 是一种用于实现高性能持久化存储的数据结构，它并不是严格意义上的顺序写。

LSM Tree 的特点是将写入操作转化为追加写（append-only write）操作。当数据需要写入时，LSM Tree 将数据追加到一个顺序写的日志（log）中，而不是直接写入到主存储结构中。这个日志被称为日志结构文件（Log Structured File）。然后，通过后台的合并（merge）和压缩（compaction）过程来定期清理和整理存储中的数据，将数据从日志结构文件中合并到主存储结构中，以提供高效的查询和读取操作。

尽管 LSM Tree 在写入时使用了顺序写的思想，但在实际执行过程中，由于需要对数据进行合并和整理，涉及到了随机写的操作。合并和压缩过程需要根据数据的键值进行查找、排序和合并操作，因此会有一些随机写操作。因此，可以说 LSM Tree 是一种混合了顺序写和随机写的数据结构。

LSM Tree 的设计目标是在较低的延迟和高写入吞吐量之间取得平衡，适用于写密集和读取较少的场景，例如许多数据库和分布式文件系统中的写入操作。

# xxx

[DDIA 读书逐章分享——第三章（上）：LSM-Tree 和 B-Tree](https://www.bilibili.com/video/BV1mL411P72H/?spm_id_from=333.337.search-card.all.click&vd_source=e293618b61bc2e8257f8e5b2ea454d76)

[LSM-Tree（日志结构合并树）](https://www.bilibili.com/video/BV1Zz4y1r7BJ/?spm_id_from=333.337.search-card.all.click&vd_source=e293618b61bc2e8257f8e5b2ea454d76)

# xxx

日志结构能够加快写，但是会让读很慢

也可以加快读（查找树、B族树），但是会让写入较慢

为了弥补读性能，可以构建索引，但是会牺牲写入性能 并 耗费存储空间

<img src="https://p.sda1.dev/13/dd1d2eb8bf994e890e351f42f16bf8d1/image.png" style="zoom: 67%;" />

<img src="https://p.sda1.dev/13/d1faef101c03dd1c16843c9c6fde379c/image.png" style="zoom:67%;" />

<img src="https://p.sda1.dev/13/a0c52b1bbf5a0210225858decaa82878/image.png" alt="image.png" style="zoom:50%;" />

<img src="https://p.sda1.dev/13/18becfe5d595931293c1cbb1c0f95d77/yuWCrQQwkx.png$" style="zoom:50%;" />

<img src="https://p.sda1.dev/13/3ae9089a0c6fe41680892aa6c94a39a4/qsqq8e2oHR.png" style="zoom:50%;" />

<img src="https://p.sda1.dev/13/690f20ac6c53e29eb9d0f86df7334c04/94pI4c3g4J.png" style="zoom:50%;" />

<img src="https://p.sda1.dev/13/8b04f83a40c5cc4953aa8a60b02888a7/7EJsevwPUJ.png" style="zoom:67%;" />

<img src="https://p.sda1.dev/13/9a0e49545123bf4ab3245f125bfc3795/rb9bVQUbB5.png" style="zoom:50%;" />

<img src="https://p.sda1.dev/13/2fb183dfd2d044066a126fe29fdb9021/AAZDGyy271.png" style="zoom:67%;" />



log-structured merge-tree 日志结构合并树

插入效率高。

维护的是键值对。

能让我们顺序写磁盘，从而大幅提升写的性能。

<img src="D:\Felix\快速笔记\assets\image-20230914221059973.png" alt="image-20230914221059973" style="zoom:50%;" />

<img src="D:\Felix\快速笔记\assets\image-20230914221131474.png" alt="image-20230914221131474" style="zoom:33%;" />

<img src="D:\Felix\快速笔记\assets\image-20230919162852659.png" alt="image-20230919162852659" style="zoom:50%;" />

<img src="D:\Felix\快速笔记\assets\image-20230914222345618.png" alt="image-20230914222345618" style="zoom:50%;" />

<img src="D:\Felix\快速笔记\assets\image-20230914222659394.png" alt="image-20230914222659394" style="zoom:33%;" />
