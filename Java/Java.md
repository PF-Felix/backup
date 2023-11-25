# SPI

> 全称是 Service Provider Interface

## JavaSPI

JavaSPI 是面向接口的，能够通过接口自动找到并加载其实现类，代码示例如下：（加入 mysql 驱动 jar 包）

```java
import java.sql.Driver;
import java.util.ServiceLoader;

public class Test {
    public static void main(String[] args) {
        ServiceLoader<Driver> loader = ServiceLoader.load(Driver.class);
        for (Driver item : loader) {
            System.out.println("Get class:" + item.getClass());
        }
    }
}
```

输出：

```
Get class:class com.mysql.jdbc.Driver
```

**原理**

扫描`META-INF\services`目录下的所有名为`java.sql.Driver`的文件，文件中定义了`java.sql.Driver`这个类的实现类，类加载器将加载所有这些实现类

**验证**

新建`META-INF\services\java.sql.Driver`文件，文件内容为`test.MysqlDriver`

定义类 MysqlDriver 如下：

```java
package test;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class MysqlDriver implements Driver {
    //...
}
```

再次执行示例代码，输出：
```
Get class:class test.MysqlDriver
Get class:class com.mysql.jdbc.Driver
```

**JavaSPI破坏了双亲委派机制**

## SpringBootSPI

SPI 的另一个例子就是 springboot，容器启动过程中会实例化`spring.factories`文件中定义的 Bean

# Integer

Integer 预先初始化一个数组常量，缓存了`-128~127`这 256 个整型数字（Long、Short 同样如此）

int 自动转化为 Integer 即自动装箱，Integer 自动转化为 int 即自动拆箱

- 自动装箱会调用 valueOf 方法，上述 256 个数字之外的其他整型数字自动装箱是需要创建 Integer 对象的
- 自动拆箱会调用 intValue 方法返回 Integer 包装的 int 数字

```java
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);
}
```

下面举例说明自动装箱与自动拆箱

```java
public class Main {
    public static void main(String[] args) {
        int i = 128;
        Integer i2 = 128;  // 自动装箱，128 不在缓存范围内，创建 Integer 对象
        Integer i3 = new Integer(128);  // 创建 Integer 对象

        System.out.println(i == i2);  // i2 自动拆箱，true
        System.out.println(i == i3);  // i3 自动拆箱，true
        Integer i5 = 127;  // 自动装箱，得到缓存数据
        Integer i6 = 127;  // 自动装箱，得到缓存数据
        System.out.println(i5 == i6);  // 是同一个对象，true

        Integer ii5 = new Integer(127);  // 创建新的 Integer 对象
        System.out.println(i5 == ii5);  // 不是一个对象，比较地址，false
        Integer i7 = new Integer(128);  // 创建新对象
        Integer i8 = new Integer(128);  // 创建新对象
        System.out.println(i7 == i8);  // 不是一个对象，比较地址，false
    }
}

public class IntegerAndIntTest {
    public static void main(String[] args) {
        int a = 1;
        Integer b = 1;  // 自动装箱，取缓存
        Integer c = 1;  // 自动装箱，取缓存
        Integer d = new Integer(1);  // 创建新对象
        Integer e = new Integer(1);  // 创建新对象
        Integer f = 130;  // 自动装箱，不在缓存范围内，创建新对象
        Integer g = 130;  // 自动装箱，不在缓存范围内，创建新对象
        System.out.println(b == d);  // 两个对象比地址，false
        System.out.println(b == c);  // 同一个对象比地址，true
        System.out.println(f == g);  // 两个对象比地址，false
        System.out.println(d == e);  // 两个对象比地址，false
        System.out.println(a == b);  // 自动拆箱后数值相等，true
        System.out.println(a == d);  // 自动拆箱后数值相等，true
    }
}
```

总结：

- Integer 与 new Integer 一定不相等，因为比较的是对象地址
- 两个 Integer 都是 new 出来的，肯定不相等
- 两个数值相等且都是非 new 出来的 Integer 对象，如果在缓存界限内则相等，否则不相等
- int 和 Integer 无论怎么比，都为 true，因为会把 Integer 自动拆箱为 int 再去比

# ==和equals有什么区别

在 Object 对象中，equals 与 == 意义是一样的

- 如果是基本数据类型，比较存储的值
- 如果是引用类型，比较的是对象的堆内存地址

# 值传递与引用传递

> 是值传递还是引用传递不重要，重要的是对方法参数的操作会不会影响原对象

- 基本数据类型（包括装箱的）：传递的是原值的拷贝，无论做什么操作都不会影响原值
- 字符串：传递的是引用副本，仍然指向原对象，但因为字符串的不可变性，操作此引用对原字符串对象无影响
- 一般的对象：传递的是引用副本，仍然指向原对象，操作此引用可能会影响原对象

# 为什么重写equals方法必须重写hashcode方法

javadoc中的描述是：
- 同一个对象的 hashCode 方法，必须返回相同的整数
- 相等（equals）的两个对象必须有相同的 hashCode
- 不相等的两个对象，不要求他们的 hashCode 必须不同
- 推论：hashCode 相同，对象可能相等也可能不等；hashCode 不同，对象肯定不相等

集合中新增元素时需要判断重复数据，会先比较 hashcode，这样更加高效，用 HashSet 举例
- 如果 hashcode 不同（对象肯定不相等），则新增元素
- 如果 hashcode 相同，再用 equals 来比较

# 基本数据类型

![image-20230425142940677](C:\ImageA\image-20230425142940677.png)