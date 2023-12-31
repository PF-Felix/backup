<img src="C:\backup\assets\image-20230919153202542.png" style="zoom: 50%;" />

> B+树进化史

# 为什么不用哈希索引

B+树的查找次数取决于树的高度，需要多次IO查询
数据库监控经常用到索引，为其创建了一个哈希索引（速度优于B+树）称为自适应哈希索引；查询的时候如果能命中自适应哈希索引就不用再使用B+树索引
是否开启此特性`innodb_adaptive_hash_index`默认为开启状态

**哈希索引的缺点**

- 只能通过等值查询定位单条数据，无法做到范围查询
- 数据量很大时，哈希冲突概率也会非常大
- 不支持利用索引排序

# 树→二叉树→二叉查找树

二叉查找树效率高，类似二分查找，二叉查找树符合以下几点：
1、左子树的所有的值小于根节点的值
2、右子树的所有的值大于或等于根节点的值

但是如果设计不好，可能形成一个不平衡的二叉查找树，树的深度太深影响查询效率

# 平衡二叉树

是一棵二叉查找树，左右两个子树的高度差不超过1，左右两个子树都是一棵平衡二叉树
维护一棵平衡二叉树的代价是非常大的

# B+树

由二叉平衡树演化而来
是一颗多叉树，树的高度远低于平衡二叉树，减少了磁盘IO次数
只有叶子节点存储数据，叶子节点由小到大（物理无序、逻辑有序、指针相连）串联在一起，叶子页中的数据也是排好序的

![image-20230406192412064](C:\backup\assets\image-20230406192412064.png)

# 为什么不用B树❓

B树每个节点都存储数据，相同数据规模下将增加磁盘IO次数，相比下来B+树磁盘IO次数少
B+树采取顺序读，B树是随机读
B+树能提高范围查询的效率，因为叶子节点指向下一个叶子节点

# B+树能存储多少个索引记录❓

MySQL B+树的节点大小等于一个页（16K），这样做的目的是每个节点只需要一次 IO 就可以完全载入

假设主键是 bigint 8字节，指针6字节，根节点可以存储索引数量 16k / 14 = 1170，第二层可以存储索引数量 1170 * 1170

假设一行数据是1K，一个叶子节点能存储 16 条数据

三层 B+ 树能够存储  1170 * 1170 * 16 大约 2000万数据