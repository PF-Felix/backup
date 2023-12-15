

# 关键概念

- bucket：桶，概念上相当于关系型数据库的 database
- measurement：概念上相当于关系型数据库的 table
- tag：标签，tag key 相当于字段名，tag value 相当于字段值
- field：存储字段名称，field 相当于字段名称
- value：存储 field 代表的字段值
- time：时间戳，可以精确到纳秒



measurement + tag + field + time 共同组成一个唯一的大 key，value 就是 value

- 时间序列：measurement + tag + field 能够定位一个时间序列，时间序列存储的是 time 和其对应的 value



# 写入数据的方式

所有的数据写入都是基于行协议的。

- 在 web UI 通过行协议写入 或 导入行协议文件（相当于批量写）
- 在 web UI 导入 CSV 文件
- 用各种客户端库 API 编写代码写数据，比如 java、python
- 利用 Telegraf 插件，无需编码写数据



# 读写最佳实践

**将数据存储在标签或字段中，而不是存储在标签键、字段键或度量中**这个就有点废话了

**哪些信息适合存为标签，哪些信息适合存为字段**

标签是建立了索引的，利用标签可以方便的筛选数据，查询引擎无需遍历每条记录查标签值
字段不带索引，需要遍历查找





将值有限的数据存储在标签中，比如枚举类型；将可变的数值存储在字段中，比如随机可变的数字

measurement 标签key 做到尽量简单化，做到不可拆分，要表示一个最小单元。比如下面例子

```markdown
# 建议
weather_sensor,crop=blueberries,plot=1,region=north temp=50.1 1472515200000000000
weather_sensor,crop=blueberries,plot=2,region=midwest temp=49.8 1472515200000000000
# 不建议
blueberries.plot-1.north temp=50.1 1472515200000000000
blueberries.plot-2.midwest temp=49.8 1472515200000000000
# 不建议
weather_sensor blueberries.plot-1.north.temp=50.1 1472515200000000000
weather_sensor blueberries.plot-2.midwest.temp=49.8 1472515200000000000
```

因为这会影响到查询，好的方案可以根据标记轻松筛选数据，坏的方案则需要使用正则表达式才能提取信息，比如下面

而且下面坏的方案是很难运用统计函数的比如平均值

```
from(bucket:"example-bucket")
    |> range(start:2016-08-30T00:00:00Z)
    |> filter(fn: (r) =>  r._measurement == "weather_sensor" and r.region == "north" and r._field == "temp")
    |> mean()
from(bucket:"example-bucket")
    |> range(start:2016-08-30T00:00:00Z)
    |> filter(fn: (r) =>  r._measurement =~ /\.north$/ and r._field == "temp")
    |> mean()
```

measurement 和 key 避免使用关键字和特殊字符

避免标签和字段的 key 重复




需要经常作为查询条件 filter 或者分组条件 group 的存储为标签

数字类型需要做统计的可以存储为字段，注意：标签只能存储字符串类型的

**高序列基数**

包含高度可变信息的标签会导致大量序列，也称为高序列基数。高序列基数是许多数据库工作负载高内存使用率的主要驱动因素。

influxdb.cardinality() 这个函数可以得到序列的数量 TODO

标签的枚举项目不宜过多，比如用户ID，如果把用户id作为标签存储，当用户数量涨到十万级别的时候，可能会开始引起问题

标签不应该只存储唯一值，比如将日志消息写入标签，时间是key 日志是value。
也可以这么理解，一个标签应该可以筛选出一组数据而不是仅仅得到一个满足条件的。

以下示例 Flux 查询显示哪些标签对基数的贡献最大。查找值比其他标签高几个数量级的标签。

```
// Count unique values for each tag in a bucket
import "influxdata/influxdb/schema"

cardinalityByTag = (bucket) => schema.tagKeys(bucket: bucket)
    |> map(
        fn: (r) => ({
            tag: r._value,
            _value: if contains(set: ["_stop", "_start"], value: r._value) then
                0
            else
                (schema.tagValues(bucket: bucket, tag: r._value)
                    |> count()
                    |> findRecord(fn: (key) => true, idx: 0))._value,
        }),
    )
    |> group(columns: ["tag"])
    |> sum()

cardinalityByTag(bucket: "example-bucket")
```

如果上面查询超时的话，就做下面的一个一个标签来看

```
import "influxdata/influxdb/schema"
schema.tagKeys(bucket: "example-bucket")


import "influxdata/influxdb/schema"
tag = "example-tag-key"
schema.tagValues(bucket: "my-bucket", tag: tag)
    |> count()
```

**如何优化写入**

批量写入，减少网络开销

在将数据点写入 InfluxDB 之前，请按字典顺序按键对标签进行排序。验证排序结果是否与 Go `bytes.Compare` 函数的结果匹配。

默认情况下，InfluxDB 以纳秒级精度写入数据。但是，如果您的数据不是在纳秒内收集的，则无需以该精度写入。为了获得更好的性能，请对时间戳使用尽可能粗的精度。

使用网络时间协议 （NTP） 在主机之间同步时间。如果时间戳未包含在线路协议中，InfluxDB 将使用其主机的本地时间（UTC 格式）为每个点分配时间戳。如果主机的时钟未与 NTP 同步，则时间戳可能不准确。

**怎么处理重复的数据**

InfluxDB通过测量，标签集和时间戳（每个都是用于将数据写入InfluxDB的Line协议的一部分）来识别唯一的数据点。

对于具有相同测量名称、标记集和时间戳的点，InfluxDB 会创建旧字段集和新字段集的联合。对于任何匹配的字段键，InfluxDB 使用新点的字段值。例如：

```
# Existing data point
web,host=host2,region=us_west firstByte=24.0,dnsLookup=7.0 1559260800000000000

# New data point
web,host=host2,region=us_west firstByte=15.0 1559260800000000000

# Resulting data point
web,host=host2,region=us_west firstByte=15.0,dnsLookup=7.0 1559260800000000000
```

可以递增时间戳来保留旧数据

```
# Old data point
web,host=host2,region=us_west firstByte=24.0,dnsLookup=7.0 1559260800000000000

# New data point
web,host=host2,region=us_west firstByte=15.0 1559260800000000001
```

**下推**

下推是将数据操作推送到基础数据源而不是对内存中的数据进行操作的函数或函数组合。使用下推查询可以提高查询性能。非下推的查询会将数据拉入内存并在那里运行所有后续操作。

一旦非下推函数运行，Flux 就会将数据拉入内存并在那里运行所有后续操作。

OSS 的这些操作是不支持下推的。

<img src="C:\backup\assets\image-20230913115454673.png" alt="image-20230913115454673" style="zoom:33%;" />

避免在 filter 中做计算 TODO

```
//这里将无法使用下推
from(bucket: "example-bucket")
    |> range(start: -1h)                      
    |> filter(fn: (r) => r.region == v.provider + v.region)
//这里可以使用下推    
region = v.provider + v.region
from(bucket: "example-bucket")
    |> range(start: -1h)                      
    |> filter(fn: (r) => r.region == region)
```

下面就不是下推相关了。

开窗时间越短就需要越多的算力，应该避免窗口持续时间较短

谨慎使用 heavy function

以下函数比其他函数使用更多的内存或 CPU。在使用它们之前，请考虑它们在数据处理中的必要性：

map() 地图（）
reduce() reduce（）
join() join（）
union() 联合（）
pivot() 枢轴（）

如果将列值设置为预定义的静态值，请使用 `set()` 动态值用map

```
data
    |> map(fn: (r) => ({ r with foo: "bar" }))
// Recommended
data
    |> set(key: "foo", value: "bar")
    
data
    |> map(fn: (r) => ({ r with foo: r.bar }))
```

<img src="C:\backup\assets\image-20230913135342629.png" alt="image-20230913135342629" style="zoom: 33%;" />

<img src="C:\backup\assets\image-20230913135504642.png" alt="image-20230913135504642" style="zoom:33%;" />

# 哈哈

当前，时序数据库主要运用在运维、IOT领域。常用它完成指标的实时检测。监控场景

工业传感器数据、服务器性能指标、股价



InfluxDB 数据模型将时间序列数据组织到 buckets and measurements 中。一个 bucket 可以包含多个 measurement。

measurement 包含多个标记和字段。

measurement 是时间序列数据的逻辑分组。

给定测量中的所有点（即一个时间序列）都应具有相同的 tag

tag：存储每个点的元数据。用于标识主机、位置、站点等数据源的东西。

field：其值随时间变化

时间戳：与数据关联的时间戳。当存储在磁盘上并查询时，所有数据都按时间排序。



Point: 由其 、tagKV、valueKV 字段键和时间戳标识的单个数据记录。

Series：具有相同 measurement、标签键和标签值的一组点。一组时间序列

如下

<img src="C:\backup\assets\image-20230911094547215.png" alt="image-20230911094547215" style="zoom:50%;" />

webUI 界面：localhost：8086



通过 token 授权



安装先跳过：

<img src="C:\backup\assets\image-20230911095129006.png" alt="image-20230911095129006" style="zoom:33%;" />



写入数据：

行协议

<img src="C:\backup\assets\image-20230911095619777.png" alt="image-20230911095619777" style="zoom:50%;" />

<img src="C:\backup\assets\image-20230911095432464.png" alt="image-20230911095432464" style="zoom: 33%;" />

```
home,room=Living\ Room temp=21.1,hum=35.9,co=0i 1641024000
home,room=Kitchen temp=21.0,hum=35.9,co=0i 1641024000
home,room=Living\ Room temp=21.4,hum=35.9,co=0i 1641027600
home,room=Kitchen temp=23.0,hum=36.2,co=0i 1641027600
home,room=Living\ Room temp=21.8,hum=36.0,co=0i 1641031200
home,room=Kitchen temp=22.7,hum=36.1,co=0i 1641031200
home,room=Living\ Room temp=22.2,hum=36.0,co=0i 1641034800
home,room=Kitchen temp=22.4,hum=36.0,co=0i 1641034800
home,room=Living\ Room temp=22.2,hum=35.9,co=0i 1641038400
home,room=Kitchen temp=22.5,hum=36.0,co=0i 1641038400
home,room=Living\ Room temp=22.4,hum=36.0,co=0i 1641042000
home,room=Kitchen temp=22.8,hum=36.5,co=1i 1641042000
home,room=Living\ Room temp=22.3,hum=36.1,co=0i 1641045600
home,room=Kitchen temp=22.8,hum=36.3,co=1i 1641045600
home,room=Living\ Room temp=22.3,hum=36.1,co=1i 1641049200
home,room=Kitchen temp=22.7,hum=36.2,co=3i 1641049200
home,room=Living\ Room temp=22.4,hum=36.0,co=4i 1641052800
home,room=Kitchen temp=22.4,hum=36.0,co=7i 1641052800
home,room=Living\ Room temp=22.6,hum=35.9,co=5i 1641056400
home,room=Kitchen temp=22.7,hum=36.0,co=9i 1641056400
home,room=Living\ Room temp=22.8,hum=36.2,co=9i 1641060000
home,room=Kitchen temp=23.3,hum=36.9,co=18i 1641060000
home,room=Living\ Room temp=22.5,hum=36.3,co=14i 1641063600
home,room=Kitchen temp=23.1,hum=36.6,co=22i 1641063600
home,room=Living\ Room temp=22.2,hum=36.4,co=17i 1641067200
home,room=Kitchen temp=22.7,hum=36.5,co=26i 1641067200
```



group:

默认情况下， `from()` 返回从 InfluxDB 查询的数据，按序列（度量、标签和字段键）分组。返回的表流中的每个表代表一个组。每个表都包含数据分组所依据的列的相同值。聚合数据时，此分组非常重要。

group() 空的 group 可以取消默认分组。将数据打平

取消分组时，数据将在单个表中返回。

<img src="C:\backup\assets\image-20230911101746960.png" alt="image-20230911101746960" style="zoom:40%;" />



pivot() 类似于行转列

仪表盘也先不看了。

<img src="C:\backup\assets\image-20230911103921902.png" alt="image-20230911103921902" style="zoom:35%;" />

这里也是先不看了。

<img src="C:\backup\assets\image-20230911104841030.png" alt="image-20230911104841030" style="zoom:33%;" />

只有 OSS 版本是免费的，集群版本和 Cloud 版本是收费的。

![image-20230910094006740](C:\backup\assets\image-20230910094006740.png)

日志结构化合并树 LSM

不支持事务、删除、更新，只能插入和查询

![image-20230910145009759](C:\backup\assets\image-20230910145009759.png)



influx write -b gpf_test -o zmj "temperature,location=room1 value=25.5 1593122400000000000"

temperature1,location=room1 value=15.5,xxde="qwww" 1694217550000000000

# 谓词下推

<img src="C:\backup\assets\image-20230909131928382.png" alt="image-20230909131928382" style="zoom:50%;" />

# insert...into...select...from

```
from(bucket:"alarm_prod")
    |> range(start:2023-08-09T00:39:09.000000000Z, stop:2023-08-10T00:39:09.000000000Z)
    |> map(fn:(r) => ({_time: r._time, AlarmType: "WARNING", _measurement: r.sid, _field: r._field, _value: r._value}))
    |> to(bucket:"alarm_dev")

from(bucket:"collect_test")
    |> range(start:2023-08-09T00:39:09.000000000Z, stop:2023-09-10T00:39:09.000000000Z)
    |> filter(fn: (r) => r["_measurement"] == "00-E0-E4-24-07-59" or r["_measurement"] == "00-E0-E4-7A-22-A3")
    |> map(fn:(r) => ({_time: r._time, _measurement: "t1", _field: "value", sid: r.sid, _value: r._value}))
    |> to(bucket:"test1")

from(bucket:"collect_test")
	|> range(start:2023-08-29T13:00:00.350338600Z, stop:2023-08-29T15:35:43.350338600Z)
	|> filter(fn: (r) => r["gentime"] =~ /2023-08-29/)
```

# 原理

数据写入 WAL 以获得即时持久性。

为了立即可供查询，points 也会写入缓存。

缓存以 TSM 文件的形式定期写入磁盘。

随着 TSM 文件的累积，存储引擎将它们合并并压缩为更高级别的 TSM 文件。



预写日志 Write Ahead Log (WAL) 类似 mysql 的 redolog，是为了保证数据的持久化。

- 存储引擎收到写入请求时，写入请求附加到 WAL 末尾
- 使用 将数据 `fsync()` 写入磁盘。（？）
- 缓存更新。？
- 数据成功写入磁盘后，响应将确认写入请求成功。
- 当存储引擎重新启动时，WAL 文件将读回内存中。

缓存

- 相当于是 WAL 的一个副本。但是两者不交互
- 当存储引擎重新启动时，WAL 文件将读回内存中。缓存从 WAL 获取更新

将字段安全地存储在 TSM 文件中后，WAL 将被截断并清除缓存。压缩过程将创建读取优化的 TSM 文件。



Time Series Index (TSI)
时间序列指数 （TSI）
存储时间序列键




我们每天有一个分片。,这样以后删除旧数据就更有。效率了

<img src="C:\backup\assets\image-20230915003053502.png" alt="image-20230915003053502" style="zoom:33%;" />

所以Lsm树的一个问题是删除删除非常昂贵。所以当你进行删除时，实际上是将一个逻辑删除写入磁盘。然后稍后，当你查询时。你必须使用逻辑删除解析数据库中的键，才能实际返回真正的结果。然后稍后，将运行压缩过程，将您的SS表重写为。要删除那个记录，对吧，基本上删除单个记录，最终会重写一堆其他记录，这可能会变得很昂贵。





