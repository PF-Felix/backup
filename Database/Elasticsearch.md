> 版本：elasticsearch-7.17.0

[版本兼容](https://www.elastic.co/cn/support/matrix#matrix_jvm)

[ES 与 SpringBoot 的版本兼容](https://docs.spring.io/spring-data/elasticsearch/docs/4.2.1/reference/html/#preface.versions)

[官网下载](https://www.elastic.co/cn/downloads/)

# 🥇核心知识篇

## 🥈环境安装（windows单机版）

Elasticsearch：解压即安装，双击 elasticsearch.bat 启动即可
验证服务启动是否成功：http://localhost:9200

Kibana：解压即安装，双击 kibana.bat 启动即可
验证服务是否启动成功：http://localhost:5601，这个地址是可以在配置文件中配置的

## 🥈_cat

`GET /_cat/nodes`查看所有节点
`GET /_cat/health`查看ES健康状况
`GET /_cat/master`查看主节点
`GET /_cat/indices`  or  `GET /_cat/indices?v`查看所有索引信息

## 🥈一般语法

```shell
#创建索引/删除索引
PUT /gpf
DELETE /gpf

#插入数据/修改数据
#修改的话是全量修改，即使提交数据与源数据完全一样也会修改，_version 和 _seq_no 都会+1
PUT /gpf/_doc/1
{
  "name": "John",
  "age": 18,
  "friend": "Tom"
}

#只修改某个字段，若提交数据与源数据完全一样，什么都不做
POST /gpf/_update/1
{
  "doc": {
    "name": "Tom3"
  }
}

#查询数据/删除数据
GET /gpf/_doc/1
DELETE /gpf/_doc/1

#查询某索引全部数据，下面两个查询效果相同
GET /gpf/_search
GET /gpf/_search
{
  "query": {
    "match_all": {}
  }
}
```

上面是在 Kibana 使用的语法，在 Postman 同样可以测试，如下图
![image-20230411233509733](https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20230411233509733.png)

## 🥈乐观锁并发控制策略

方法1：`PUT gpf/_doc/2?version=9&version_type=external`
方法2：`PUT gpf/_doc/2?if_seq_no=38&if_primary_term=3`

version 是文档级别的，seq_no 是索引级别的

## 🥈bulk批量操作

```shell
#新增一条记录
POST _bulk
{"create":{"_index": "gpf","_id":1}}
{"name": "gaga create"}

#删除一条记录
POST _bulk
{"delete":{"_index": "gpf","_id":1}}

#修改一条记录，部分修改
POST _bulk
{"update":{"_index": "gpf","_id":1}}
{"doc": {"name": "gaga1"}}
```

这种操作无法使用 Postman 提交数据了

## 🥈搜索与查询

### _source

```shell
#不查询任何字段
GET /gpf/_search
{
  "_source": false
}

#只查询name字段
GET /gpf/_search
{
  "_source": "name"
}

#查询多个字段
GET /gpf/_search
{
  "_source": ["name","age","job.company"]
}
```

### queryByUrl

通过 url 发送查询参数

```shell
#检索name字段，查询效果同match
GET /gpf/_search?q=name:Tom

#检索所有的字段
GET /gpf/_search?q=Tom

#分页查询
GET /gpf/_search?from=0&size=2&sort=age:desc
```

### match

注意：查询条件和数据源都会分词
例外：如果使用 keyword 的话，查询条件和数据源都不会分词
PS：分词器分词可能转换大小写

#### match

```shell
#任意一个词项匹配数据源词项即可
GET /gpf/_search
{
  "query": {
    "match": {
      "name": "Tom"
    }
  }
}
GET /gpf/_search
{
  "query": {
    "match": {
      "name": "Tom John"
    }
  }
}
```

#### match_phrase

```shell
#短语匹配，所有词项都需要匹配数据源词项，且顺序一致
#"Tom Hebe" 无法匹配 "Tom Tim Hebe"
GET /gpf/_search
{
  "query": {
    "match_phrase": {
      "name": "Tom Hebe"
    }
  }
}
```

#### multi_match

```shell
#任意一个词项匹配数据源词项即可
#name=Tom name=John friend=Tom friend=John都可以匹配
GET /gpf/_search
{
  "query": {
    "multi_match": {
      "query": "Tom John",
      "fields": ["name","friend"]
    }
  }
}
```

##### 评分相关的参数

数据准备：

```shell
DELETE product
PUT product
{
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "desc": {
        "type": "text",
        "analyzer": "ik_max_word"
      }
    }
  }
}
PUT /product/_doc/1
{
  "name": "chiji shouji，游戏神器，super ",
  "desc": "基于TX深度定制，流畅游戏不发热，物理外挂，charge",
  "price": 3999,
  "createtime": "2020-05-20",
  "collected_num": 99,
  "tags": [
    "性价比",
    "发烧",
    "不卡"
  ]
}
PUT /product/_doc/2
{
  "name": "xiaomi NFC shouji",
  "desc": "支持全功能NFC,专业 chiji，charge",
  "price": 4999,
  "createtime": "2020-05-20",
  "collected_num": 299,
  "tags": [
    "性价比",
    "发烧",
    "公交卡"
  ]
}
PUT /product/_doc/3
{
  "name": "NFC shouji，super ",
  "desc": "shouji 中的轰炸机",
  "price": 2999,
  "createtime": "2020-05-20",
  "collected_num": 1299,
  "tags": [
    "性价比",
    "发烧",
    "门禁卡"
  ]
}
PUT /product/_doc/4
{
  "name": "xiaomi 耳机",
  "desc": "耳机中的黄焖鸡",
  "price": 999,
  "createtime": "2020-05-20",
  "collected_num": 9,
  "tags": [
    "低调",
    "防水",
    "音质好"
  ]
}
PUT /product/_doc/5
{
  "name": "红米耳机",
  "desc": "耳机中的肯德基",
  "price": 399,
  "createtime": "2020-05-20",
  "collected_num": 0,
  "tags": [
    "牛逼",
    "续航长",
    "质量好"
  ]
}

DELETE teacher
POST /teacher/_bulk
{"index":{"_id":"1"}}
{"name":{"姓":"吴","名":"磊"}}
{"index":{"_id":"2"}}
{"name":{"姓":"连","名":"鹏鹏"}}
{"index":{"_id":"3"}}
{"name":{"姓":"张","名":"明明"}}
{"index":{"_id":"4"}}
{"name":{"姓":"周","名":"志志"}}
{"index":{"_id":"5"}}
{"name":{"姓":"吴","名":"亦凡"}}
{"index":{"_id":"6"}}
{"name":{"姓":"吴","名":"京"}}
{"index":{"_id":"7"}}
{"name":{"姓":"吴","名":"彦祖"}}
{"index":{"_id":"8"}}
{"name":{"姓":"帅","名":"吴"}}
{"index":{"_id":"9"}}
{"name":{"姓":"连","名":"磊"}}
{"index":{"_id":"10"}}
{"name":{"姓":"周","名":"磊"}}
{"index":{"_id":"11"}}
{"name":{"姓":"张","名":"磊"}}
{"index":{"_id":"12"}}
{"name":{"姓":"马","名":"磊"}}
```

**most_fields**【最多的字段】某个term匹配到的**field越多**评分越高
**best_fields**【最好的字段】某个field匹配到的**term越多**评分越高

**tie_breaker**0.3表示name权重0.7剩余字段权重0.3【带不带这个参数比较一下结果】

**cross_fields**
词频（TF）：关键词在每个doc中出现的次数，词频越高，评分越高
反词频（IDF）：关键词在整个索引中出现的次数，反词频越高，评分越低
每个doc的长度，越长相关度评分越低

查询例子：

| <img src="https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20231020153621500.png" alt="image-20231020153621500" style="zoom:45%;" /> | <img src="https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20231020153654779.png" alt="image-20231020153654779" style="zoom:45%;" /> | <img src="https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20231020153718179.png" alt="image-20231020153718179" style="zoom:45%;" /> |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |

| <img src="https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20231020153738749.png" alt="image-20231020153738749" style="zoom:45%;" /> | <img src="https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20231020154012976.png" alt="image-20231020154012976" style="zoom:38%;" /> |
| ------------------------------------------------------------ | ------------------------------------------------------------ |

上面图片和下面代码内容一样。

```shell
GET product/_search
{
  "query": {
    "multi_match": {
      "query": "chiji shouji",
      "type": "most_fields",
      "fields": [
        "name",
        "desc"
      ]
    }
  }
}

GET product/_search
{
  "query": {
    "multi_match": {
      "query": "chiji shouji",
      "type": "best_fields",
      "fields": [
        "name",
        "desc"
      ]
    }
  }
}

GET product/_search
{
  "query": {
    "multi_match": {
      "query": "super charge",
      "type": "best_fields",
      "fields": [
        "name",
        "desc"
      ],
      "tie_breaker": 0.3
    }
  }
}

GET teacher/_search
{
  "query": {
    "multi_match": {
      "query": "吴磊",
      "type": "most_fields",
      "fields": [
        "name.姓",
        "name.名"
      ]
    }
  }
}

#上个案例中
#在整个索引中，吴字作为姓非常常见，磊字作为名非常常见（反词频过高），帅字作为姓非常少见（反词频过低）
#因此我们期望吴磊得分高，但事实却是帅磊得分高
#解决方案：按整体查询
GET teacher/_search
{
  "query": {
    "multi_match": {
      "query": "吴磊",
      "type": "cross_fields",
      "fields": [
        "name.姓",
        "name.名"
      ],
      "operator": "and"
    }
  }
}
```

### term

- 数据源分词
- 查询条件不分词（输入什么样就是什么样，分词器分词可能转换大小写）（不分词就不会建立索引）
- 查询条件作为一个词项，去匹配数据源的每个词项

```shell
GET /gpf/_search
{
  "query": {
    "term": {
      "name": {
        "value": "tom john"
      }
    }
  }
}

GET /gpf/_search
{
  "query": {
    "terms": {
      "name": [
        "tom hebe",
        "hulu"
      ]
    }
  }
}
```

### range

```shell
GET /gpf/_search
{
  "query": {
    "range": {
      "age": {
        "gte": 18,
        "lte": 20
      }
    }
  }
}
```

### bool

组合查询

```shell
#A且B，计算得分
GET /gpf/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "name": "hebe"
          }
        },
        {
          "match_phrase": {
            "name": "tom john"
          }
        }
      ]
    }
  }
}

#非A且非B，不计算分数
GET /gpf/_search
{
  "query": {
    "bool": {
      "must_not": [
        {
          "match": {
            "name": "tom"
          }
        },
        {
          "match_phrase": {
            "name": "Hulu"
          }
        }
      ]
    }
  }
}

#A或B，计算得分
#minimum_should_match参数指定should必须匹配的子句数量，默认为1
GET /gpf/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "match": {
            "name": "hulu"
          }
        },
        {
          "match_phrase": {
            "name": "tom john"
          }
        }
      ]
    }
  }
}

#A且B，不计算分数（filter，所有的过滤查询都不计算分数）
GET /gpf/_search
{
  "query": {
    "bool": {
      "filter": [
        {
          "match": {
            "name": "hebe"
          }
        },
        {
          "match_phrase": {
            "name": "tom john"
          }
        }
      ]
    }
  }
}
```

## 🥈聚合查询

用于聚合的查询条件字段不能被分词

### 分桶聚合

类似于数据库的分组查询

```shell
GET /gpf/_search
{
  "size": 0,
  "aggs": {
    "NAME1": {
      "terms": {
        "field": "age",
        "size": 2,
        "order": {
          "_count": "desc"
        }
      }
    }
  }
}
GET /gpf/_search
{
  "aggs": {
    "NAME2": {
      "terms": {
        "field": "name.keyword",
        "size": 10
      }
    }
  }
}
```

### 指标聚合

### 嵌套聚合

```shell
#举例1：先查询再聚合，与query和aggs的顺序无关
GET /gpf/_search
{
  "query": {
    "match": {
      "name": "tom"
    }
  },
  "aggs": {
    "NAME1": {
      "terms": {
        "field": "name.keyword"
      },
      "aggs": {
        "NAME2": {
          "avg": {
            "field": "age"
          }
        }
      }
    }
  }
}

#举例2：使用了global的聚合查询不再依赖query的查询结果
GET /gpf/_search
{
  "query": {
    "term": {
      "name.keyword": {
        "value": "Tom"
      }
    }
  },
  "aggs": {
    "NAME1": {
      "terms": {
        "field": "name.keyword"
      }
    },
    "NAME2": {
      "global": {},
      "aggs": {
        "NAME3": {
          "terms": {
            "field": "name.keyword"
          }
        }
      }
    }
  }
}

#举例3：嵌套聚合排序，外层优先排序
GET /gpf/_search
{
  "aggs": {
    "NAME1": {
      "terms": {
        "field": "age",
        "order": {
          "_count": "asc"
        }
      },
      "aggs": {
        "NAME2": {
          "terms": {
            "field": "name.keyword",
            "order": {
              "_count": "desc"
            }
          }
        }
      }
    }
  }
}

#举例4：嵌套聚合排序，指定以某层查询结果排序
GET /gpf/_search
{
  "size": 0,
  "aggs": {
    "NAME1": {
      "terms": {
        "field": "name.keyword",
        "order": {
          "NAME2.value": "desc"
        }
      },
      "aggs": {
        "NAME2": {
          "sum": {
            "field": "age"
          }
        }
      }
    }
  }
}
```

### 管道聚合

```shell
#求平均年龄最大的同名的人
GET /gpf/_search
{
  "size": 0,
  "aggs": {
    "NAME1": {
      "terms": {
        "field": "name.keyword"
      },
      "aggs": {
        "NAME2": {
          "avg": {
            "field": "age"
          }
        }
      }
    },
    "NAME3": {
      "max_bucket": {
        "buckets_path": "NAME1 > NAME2"
      }
    }
  }
}
```

## 🥈模糊查询

包括前缀搜索、通配符、正则表达式、模糊查询等，基本用不到用的时候再看

参考语雀

## 🥈映射

## 🥈脚本查询

没有用过，感觉用不到，DSL已经很强大了

参考语雀

## 🥈分析器

### 常见的中文分析器

```shell
GET custom_analysis/_analyze
{
  "analyzer": "ik_max_word",
  "text": [
    "我爱中华人民共和国"
  ]
}

GET custom_analysis/_analyze
{
  "analyzer": "ik_max_word",
  "text": [
    "蒙丢丢",
    "大G",
    "霸道",
    "渣男",
    "渣女",
    "奥巴马"
  ]
}

GET custom_analysis/_analyze
{
  "analyzer": "ik_max_word",
  "text": [
    "吴磊",
    "美国",
    "日本",
    "澳大利亚"
  ]
}
```

ES 还可以自定义分析器，自定义分析器包含过滤器

### 过滤器

> filter

过滤器用于分词之前预处理，过滤无用字符
可以做到：大小写转换、词项转换、语气词处理

```shell
#《html_strip》
DELETE my_index
PUT my_index
{
  "settings": {
    "analysis": {
      "char_filter": {
        "my_char_filter": {
          "type": "html_strip",
          "escaped_tags": ["a"]
        }
      },
      "analyzer": {
        "my_analyzer": {
          "tokenizer": "keyword",
          "char_filter": ["my_char_filter"]
        }
      }
    }
  }
}
GET my_index/_analyze
{
  "analyzer": "my_analyzer",
  "text": "<p>I&apos;m so <a>happy</a>!</p>"
}

#《mapping》
DELETE my_index
PUT my_index
{
  "settings": {
    "analysis": {
      "char_filter": {
        "my_char_filter": {
          "type": "mapping",
          "mappings": [
            "滚 => *",
            "垃 => *",
            "圾 => *"
          ]
        }
      },
      "analyzer": {
        "my_analyzer": {
          "tokenizer": "keyword",
          "char_filter": [
            "my_char_filter"
          ]
        }
      }
    }
  }
}
GET my_index/_analyze
{
  "analyzer": "my_analyzer",
  "text": "你就是个垃圾！滚"
}

#《pattern replace》
DELETE my_index
PUT my_index
{
  "settings": {
    "analysis": {
      "char_filter": {
        "my_char_filter": {
          "type": "pattern_replace",
          "pattern": """(\d{3})\d{4}(\d{4})""",
          "replacement": "$1****$2"
        }
      },
      "analyzer": {
        "my_analyzer": {
          "tokenizer": "keyword",
          "char_filter": ["my_char_filter"]
        }
      }
    }
  }
}
GET my_index/_analyze
{
  "analyzer": "my_analyzer",
  "text": "您的手机号是17611001200"
}

#《synonym同义词替代》
DELETE test_index
PUT /test_index
{
  "settings": {
    "analysis": {
      "filter": {
        "my_synonym": {
          "type": "synonym",
          "synonyms": [
            "赵,钱,孙,李=>吴",
            "周=>王"
          ]
        }
      },
      "analyzer": {
        "my_analyzer": {
          "tokenizer": "standard",
          "filter": [
            "my_synonym"
          ]
        }
      }
    }
  }
}
GET test_index/_analyze
{
  "analyzer": "my_analyzer",
  "text": [
    "赵,钱,孙,李",
    "周"
  ]
}
```

### 分词器

> tokenizer

常见分词器：
standard：默认分词器，中文支持的不理想，会逐字拆分
pattern：以正则匹配分隔符，把文本拆分成若干词项
whitespace：以空白符分隔

自定义分词器：

```shell
DELETE custom_analysis
PUT custom_analysis
{
  "settings": {
    "analysis": {
      "char_filter": {
        "my_char_filter": {
          "type": "mapping",
          "mappings": [
            "& => and",
            "| => or"
          ]
        },
        "html_strip_char_filter": {
          "type": "html_strip",
          "escaped_tags": [
            "a"
          ]
        }
      },
      "filter": {
        "my_stopword": {
          "type": "stop",
          "stopwords": [
            "is",
            "in",
            "the",
            "a",
            "at",
            "for"
          ]
        }
      },
      "tokenizer": {
        "my_tokenizer": {
          "type": "pattern",
          "pattern": "[ ,.!?]"
        }
      },
      "analyzer": {
        "my_analyzer": {
          "type": "custom",
          "char_filter": [
            "my_char_filter",
            "html_strip_char_filter"
          ],
          "filter": [
            "my_stopword",
            "lowercase"
          ],
          "tokenizer": "my_tokenizer"
        }
      }
    }
  }
}
GET custom_analysis/_analyze
{
  "analyzer": "my_analyzer",
  "text": [
    "What is ,<a>as.df</a>  ss<span> in ? &</span> | is ! in the a at for "
  ]
}
```

# 🥇高手进阶篇

## 🥈基本概念

索引：相当于关系型数据库中表的概念
索引类型：ES7 移除了 type 的概念，现在直接建议写成 _doc，但并没有禁止使用别的类型（ES 会给一个警告）
文档：一个文档类似于关系型数据库中的一行数据，每个文档都有一个ID
字段：文档中的字段相当于关系型数据库表中的列

映射（mapping）：定义了文档包含哪些字段、字段类型、分析器、过滤器（字符过滤器、令牌过滤器）、分词器等

## 🥈倒排索引

上面已经说过：ES 的索引相当于关系型数据库的表

ES 对索引的每个字段都做了倒排索引

倒排索引通过分词策略，形成了词项和文章的映射关系表

倒排索引包括词项字典（Term Dictionary）和倒排列表（Posting List）
![image-20230412145602950](https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20230412145602950.png)

词项字典：词项的集合，存储在 tim 文件中
词项：一段文本经过分析器分析之后得到一个个词项，每个词项指向一个倒排列表
倒排列表：记录词项在所有文档中出现的位置即ID、词频、偏移量；所有词项的倒排列表顺序存储在磁盘文件中

ES 可以像 Mysql 一样，使用 B+树建立索引字典指向倒排列表，这样就可以有 Mysql 一样的查询时间复杂度
为了少读磁盘（索引存在磁盘），就必须将索引放到内存，但是词项字典太大了，于是就有了 Term Index
ES使用**Term Index**数据结构是 FST，是一个有向无环图，存储于 tip 文件中，需要内存加载
1、因为在内存中所以查询速度快
2、占用空间小，通过对单词前缀和后缀的重复利用，极大的压缩了存储空间
FST 存储着词项在字典中的位置，在 FST 匹配到词项之后再去词项字典（磁盘 tim 文件）中查找词项，大大减少随机IO的次数

其他
1、倒排索引是通过 value 找 key，正向索引是通过 key 找 value
2、倒排列表如果不压缩将非常占用磁盘空间，ES 提供的压缩算法有 FOR 和 RBM

## 🥈深分页问题

其实 mysql 也存在这个问题，mysql 怎么解决呢？TODO

一般情况下分页查询使用**from+size**

```shell
#1、各个分片先查询排序前1005条数据
#2、聚合所有分片的查询结果（1005*分片数量），再次排序之后得到相应的查询结果
#如果分页太深，即 from 太大，堆内存中汇总的数据就过多，就容易出现内存溢出，这就是深分页问题
#为了防止内存溢出的问题，有一个参数max_result_window默认值是10000，来限制分页返回的最大数值
GET gpf/_search
{
  "from": 1000,
  "size": 5
}
```

解决方案看下文

**Search Scroll**

本质是第一次查询所有的数据并存为快照，之后的查询是查快照，因此是非实时的
而且一个时间点如果存在过多的 Scroll 依然会占用大量内存，因此只适用于非C端场景（C端是消费者，B端是企业）

使用方法：

```shell
GET /product/_search?scroll=1m
{
  "size": 2
}

GET /_search/scroll
{
  "scroll":"1m",
  "scroll_id":"DXF1ZXJ5QW5kRmV0Y2gBAAAAAAAADbcWZEs2eGgxaXFRWUtmSWc3Yk8xTHZTZw**"
}
```

**Search After**

原理：跟`from+size`比较类似
区别在于：每个分片查询的不是前 N 个记录，而是`search after`之后的`size`个记录，因此占用内存有限
缺点：只支持下一页查询

使用方法：

```shell
POST twitter/_search
{
    "size": 10,
    "query": {
        "match" : {
            "title" : "es"
        }
    },
    "sort": [
        {"date": "asc"},
        {"_id": "desc"}
    ]
}

GET twitter/_search
{
    "size": 10,
    "query": {
        "match" : {
            "title" : "es"
        }
    },
    "search_after": [124648691, "624812"], #上次查询最后一条记录的sort值
    "sort": [
        {"date": "asc"},
        {"_id": "desc"}
    ]
}
```

**分页功能怎么实现？**

其他考虑下来也就几个功能点：

- 自定义跳页功能：没有这个功能是可以的，并没有牺牲用户体验，因为用户关注的是搜索精准度，肯定按搜索结果的顺序看
  就算需要支持这个功能我可以通过限制页号最大值来避免深分页，页号太大后面的搜索结果不精准没有意义
- 自选页号功能：可以通过限制页号最大值来避免深分页，页号太大后面的搜索结果不精准没有意义
- 下一页功能：使用 Search After 实现

我们看看主流网站：

- 谷歌、百度：没有自定义跳页功能；支持自选页号，但根据测试百度网页版最多76页；支持下一页
- 淘宝：网页版支持自定义跳页但最大页是100，支持自选页号最大也是100页；手机端：通过下拉加载本质上就是下一页

因此`from+size`结合`Search After`是可以解决深分页问题的

## 🥈Master选举

### 节点类型

下文有

### 何时触发选举

情况1：活跃 master 节点数量小于法定票数
情况2：active master 挂掉

### 选举过程

<img src="https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20231020161529086.png" alt="image-20231020161529086" style="zoom: 38%;" />

法定票数：当选 Master 所需的最小票数，是可配置的，通常情况下为有效投票节点数过半

**脑裂问题**

部分节点不能与主节点正常通信，这些节点会选出一个新的主节点，集群就有了两个主节点，即脑裂

解决方案：`discovery.zen.minimum_master_nodes=N/2+1`，N为有效投票节点数；保证了只能有一部分节点的投票达到这个数字选出一个主节点，其他部分的投票均达不到这个数字选不出主节点

PS：集群通常应该有奇数个候选节点；如果是偶数的话，平分成两个部分任何一个部分都选不出主节点

## 🥈数据写入

数据写入均发生在主分片，写入完成后再同步到副本

**写数据过程**

1. 客户端选择一个节点发送请求
2. 这个节点就作为协调节点，根据文档 ID 进行路由，将请求转发给具备主分片的节点
3. 主分片节点写入成功之后，将数据同步到副本
4. 协调节点等到主分片和副本都写入成功之后，就返回响应结果给客户端

**写一致性**

一致性由参数`wait_for_active_shards`控制

- 默认值为1，即只需要主分片写入成功
- 可以设置为 all 或任何正整数，最大值为索引分片总数，如果设置为 2，就只需要一个副本分片写入成功即可；如果副本写入不成功，写操作必须等待并重试，超时时间是30秒

**写入原理**
![image-20230412161217017](https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20230412161217017.png)

## 🥈读写性能调优

### 写入调优

以提升写入吞吐量和并发能力为目标，而非提升写入实时性

**增加 buffer 大小**本质上是减少 refresh 操作

**增加 refresh 时间间隔**
目的是减少 segment 的创建，减少 segment 的 merge 次数
这些操作都发生在 JVM，大量创建对象且对象长时间存活无法回收，有可能导致频繁的 full gc 甚至内存溢出
这个方案会降低写实时性

**增加 flush 时间间隔**目的是减小数据写入磁盘的频率，减小磁盘IO频率

**禁用交换分区swap**

**使用多个工作线程**为了使用集群的所有资源，应该使用多个线程发送数据

**避免使用稀疏数据**如果同一个 index 下存储含有不同字段的文档，会影响 ES 压缩文档的能力，导致查询效率降低

**异步写事务日志**如果允许数据丢失，可以设置异步写事务日志

### 查询调优

**避免单次召回大量数据**
搜索引擎最擅长的事情是从海量数据中查询少量相关文档，而非单次检索大量文档
非常不建议查询上万数据，如果有这样的需求，建议使用滚动查询

**避免单个文档过大**

**避免深度分页**

**使用 filter 代替 query**filter 不用计算评分

**避免使用脚本**相对于 DSL  而言，脚本性能差

**使用 keyword 类型**精准匹配，如果不分词，就用

## 🥈数据库的索引

数据库的索引一般都是以索引文件的形式存储在磁盘上，每次查询时加载到内存

Mysql B+树索引并不能直接找到行，只是找到行所在的页，通过把整页读入内存，再在内存中查找，因此索引树的高度决定了磁盘IO的次数

## 🥈节点类型

候选节点：配置了 master 的节点都能参与选举和投票，集群至少三个候选节点

仅投票节点：配置了 master 和 voting_only 的节点是仅投票节点，不参与选举，同时可作为是数据节点

主节点：

- 负责创建删除索引、分片的分配等
- 应避免主节点承载其他任务
- 非仅投票节点的候选节点都有可能被选为主节点

数据节点：保存索引文档分片，处理数据相关的操作

## 🥈评分算法

影响评分的三个维度：
![image-20230426222026481](https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20230426222026481.png)

早期评分算法是 TF/IDF

后期使用的 BM25 算法主要优化了词频对评分的影响，随着词频越来越高，对评分的影响越来越小越趋近于平缓

![image-20230426221806456](https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20230426221806456.png)

## 🥈高可用ES集群

ES的高可用性：集群可容忍部分节点宕机而保持服务的可用性和数据的完整性
1、假如宕机的是Master，选举新的Master
2、Master尝试恢复故障节点
3、主分片所在节点故障，Active Master 会将某个副本提升为主分片
4、Master将宕机期间丢失的数据同步到重启节点对应的分片上去，从而使服务恢复正常
5、通过针对节点、分片的策略降低单故障点对整体服务产生的影响，例如分片分配感知中的举例

**分布式的优点**
高可用：集群可容忍部分节点宕机而保持服务的可用性和数据的完整性
高性能：负载均衡分担请求压力，大大提高集群的吞吐能力和并发能力
易扩展：当集群性能不足时，可以方便快速的扩展集群，不用停止服务

### 分片分配感知

通过自定义属性作为感知属性，如果 ES 知道哪些节点位于同一机架、同一机房或同一数据中心中，则它可以分离主副本分片，最大程度地降低故障时丢失数据的风险，需要做如下配置：

```shell
#配置节点属性
node.attr.rack_id:rack1
```

```shell
#集群级设置
PUT _cluster/settings
{
  "persistent": {
    "cluster.routing.allocation.awareness.attributes":"rack_id"
  }
}
```

举例：机房1有A、B两个节点，机房2有C节点，如果有三个分片0 1 2，主分片将平均分配到三个节点一个节点一个主分片，如果一个机房断电，必定有主分片不可用，虽然随后副本分片升级为主分片，但这个过程有短暂的时间将影响到数据写入，对生产环境有很大影响

解决方案：对机房1的节点做上面的配置，主分片将不被平均分配到三个节点，而是只分配到机房1的节点中，机房2中没有主分片存在，一个机房断电之后集群的可用率为50%

参考下图理解：
<img src="https://raw.githubusercontent.com/PF-Felix/ImageA/main/image-20230412182148065.png" alt="image-20230412182148065" style="zoom: 67%;" />

### 小规模集群

- 单节点集群不考虑
- 两节点集群，不能选主，不推荐
- 三节点集群，HA的最低配置，候选节点同时作为数据节点，业务量不大的话可以使用
- 多节点集群：三个候选节点（太多的话将影响选主的速度）；设置专用节点，候选节点与数据节点分开

### 大规模集群

单集群：

- 避免跨数据中心，ES 对网络和宽带需求较高
- 部署分片分配感知，降低单个区域（比如一个机架）节点宕机对整个服务造成影响

双区集群：

- 如果集群部署在两个区域比如两个机房，应该在每个机房部署不同的候选节点，服务可用率为50%
- 如果每个机房候选节点相同，一个机房断电将导致无法选主而使服务不可用（因此候选节点需要是奇数）

多区集群：选三个区域每个区域部署一个候选节点即可

# 🥇面试题

## 你们的集群架构、索引大小、分片多少、调优手段

集群架构：3个候选节点，3个数据节点
分片数量：3个分片
索引大小：没有计算过

调优手段：

1. 设计阶段调优
   合理的设置分词器
   映射的编写充分结合各个字段的属性，是否需要检索、是否需要存储等
2. **上文**读写性能调优
3. 根据需要部署**上文**高可用ES集群
4. 业务调优

## ES索引文档的过程

即文档写入ES，创建索引的过程，参考**上文**数据写入

## ES的搜索过程

可以参考**上文**深分页问题
1、各个分片先查询
2、协调节点聚合所有分片的查询结果返回给客户端