场景是关于逻辑删除的，0表示未删除 1表示已删除

根据 id 查询的时候就需要把**deleted=0**这个条件加上去，此时需要建立联合索引吗？

使用执行计划测试下来，加了联合索引之后依然走的是主键查询

个人理解是：主键查询已经足够快了，没有必要使用联合索引

<img src="https://raw.githubusercontent.com/PF-Felix/ImageA/main/20231008103430.png" style="zoom:33%;" />

<img src="https://raw.githubusercontent.com/PF-Felix/ImageA/main/20231008103539.png" style="zoom:50%;" />

<img src="https://raw.githubusercontent.com/PF-Felix/ImageA/main/20231008103609.png" style="zoom: 67%;" />