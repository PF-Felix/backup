# 批量操作举例

【批量插入】

```xml
<insert id="insertBillInfos" parameterType="java.util.List">
    insert into sg_sp_work_bill (
        ID, CODE, NAME, CREATE_DATE, CREATE_USER
    )
    values
    <foreach collection="list" item="item" index="index" separator=",">
    (
        #{item.id,jdbcType=DECIMAL},
        #{item.code,jdbcType=VARCHAR},
        #{item.name,jdbcType=VARCHAR},
        NOW(),
        #{item.createUser,jdbcType=DECIMAL}
    )
    </foreach>
</insert>
```

【批量删除】

```xml
<delete id="deleteMenus" parameterType="java.util.List">
    delete from t_acl_menu where id in
    <foreach collection="list" item="id" index="index" open="(" separator="," close=")">
        #{id}
    </foreach>
</delete>
```

# 面试题

## mapper的返回值

insert、update、delete 的返回值默认是插入、更新、删除的行数

insert 若想要返回主键，需要另做配置
```xml
<selectKey order="AFTER" resultType="long" keyProperty="id">
    SELECT LAST_INSERT_ID() as id
</selectKey>
```

## MyBatis与Hibernate有哪些不同

使用 Hibernate 开发可以节省很多代码，提升开发效率，hibernate 还能做到数据库无关性；但是学习门槛高

Mybatis 学习门槛低，直接编写 SQL 更加灵活，能够编写更加高效的 SQL；缺点是无法做到数据库无关性

## 缓存

一级缓存是session级别，默认开启，可以关闭
二级缓存是应用级别，默认关闭，可以开启
如何使用三级缓存？自定义缓存类，实现 cache 接口

查询顺序：先查二级缓存，查不到数据再查一级缓存

查看《MyBatis核心源码》搜索缓存继续往下说

## 实体类属性名和表字段名不一样，如何将查询结果封装到对象

方法1：在查询的 sql 语句中定义字段名的别名

方法2：通过`<resultMap>`来映射字段名和实体类属性名的关系

## 如何执行批处理

使用 BatchExecutor 完成批处理

## mapper中如何传递多个参数

方法1：直接在方法中传递参数，xml 文件用 #{0} #{1} 来获取

方法2：使用 @Param 注解，这样可以直接在 xml 文件中通过 #{name} 来获取

方法3：通过 Map 传参，xml 文件中通过 #{key} 来获取

方法4：通过自定义对象传参，xml 文件中通过 #{属性名称} 来获取

## #{}和${}的区别

${}是直接替换
#{}是预编译处理，处理 #{} 时会将它替换为 ？，调用 PreparedStatement 的 set 方法来赋值

使用 #{} 可以有效的防止 SQL 注入，提高系统安全性

## MyBatis的工作原理

## 缓存的设计

## 谈谈你对Executor的理解

Executor 的类型有三类：

- SimpleExecutor：默认的，每次操作都是一个新的 Statement 对象
- ReuseExecutor：根据 SQL 缓存 Statement 对象，实现 Statement 对象的复用
- BatchExecutor：可以实现 Statement 对象的复用，而且批处理执行 SQL

默认的 Executor 可以在配置文件中设置

## 谈谈对日志模块的理解

## 事务

JDBC：自己管理事务
Managed：自己部管理事务，可以交给 Spring 管理
