> 标签：设计模式、源码

> MyBatis 版本是 3.5.8

# 总结

1. 解析配置文件并获取 SqlSessionFactory 对象
   1. 首先 new 了一个 SqlSessionFactoryBuilder 对象用来创建 SqlSessionFactory 对象，这是建造者模式的运用
   2. 创建 XMLConfigBuilder 对象
      1. 创建 Configuration 对象，这个对象将存放所有的配置信息
      2. 设置是否已经解析的标志为 false
      3. environment 初始化
      4. XML 解析器初始化
   3. XMLConfigBuilder 对象利用 XML 解析器解析全局配置文件，将这些配置都存入 Configuration 对象
   4. XMLMapperBuilder 对象利用 XML 解析器解析 mapper 映射文件
      为每个 mapper 标签下的增删改查标签创建一个 MappedStatement 对象
      为每个 namespace 接口类型注册绑定一个 MapperProxyFactory 对象
   5. 创建 DefaultSqlSessionFactory 对象（单例），里面持有 Configuration 实例
2. 利用 SqlSessionFactory 对象获取 SqlSession 对象
   因为 SqlSession 不是线程安全的，因此每个会话都创建一个 SqlSession 对象
   SqlSession 封装了增删改查的API，对象中包含执行器、本次事务、全局配置信息
3. 利用 SqlSession 得到 namespace 接口的动态代理对象 MapperProxy（getMapper方法）
   通过上文绑定的 MapperProxyFactory 对象创建
   JDK 动态代理
4. 通过代理对象来操作数据库
   1. invoke，选择 SqlSession 对象的 session 方法执行，最终是交给执行器执行
   2. BaseExecutor 查询数据并处理一级缓存
   3. 真正查数据库的逻辑是在 SimpleExecutor，创建出的 PreparedStatement 对象执行查询
5. 关闭会话

```java
public class MyBatisSourcecodeTest {
    public static void main(String[] args) throws IOException {
        InputStream in = Resources.getResourceAsStream("mybatis-config.xml");
        //解析配置文件并获取SqlSessionFactory对象
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        //根据SqlSessionFactory对象获取SqlSession对象
        SqlSession sqlSession = factory.openSession();
        //通过SqlSession中提供的API方法来操作数据库
        UserDao mapper = sqlSession.getMapper(UserDao.class);
        System.out.println(mapper.insert(User.builder().name("Tom" + System.currentTimeMillis()).age(10).build()));
        sqlSession.commit();
        System.out.println(mapper.count(User.builder().build()));
        //关闭会话
        sqlSession.close();
    }
}
```

# 解析配置文件并创建SqlSessionFactory对象

## 时序图

![image-20230317153939334](D:\temporary\image-20230317153939334.png)

## SqlSessionFactory

```java
public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
    SqlSessionFactory var5;
    try {
        //创建XMLConfigBuilder对象
        XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
        //解析全局配置文件
        var5 = this.build(parser.parse());
    } catch (Exception var14) {
        throw ExceptionFactory.wrapException("Error building SqlSession.", var14);
    } finally {
        ErrorContext.instance().reset();

        try {
            inputStream.close();
        } catch (IOException var13) {
        }

    }

    return var5;
}

public SqlSessionFactory build(Configuration config) {
    return new DefaultSqlSessionFactory(config);
}
```

## XMLConfigBuilder

```java
private boolean parsed;
private final XPathParser parser;
private String environment;
private final ReflectorFactory localReflectorFactory;

//构造器
private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
    //Configuration初始化，包括类型别名的注册
    super(new Configuration());
    this.localReflectorFactory = new DefaultReflectorFactory();
    ErrorContext.instance().resource("SQL Mapper Configuration");
    //为Configuration设置props属性
    this.configuration.setVariables(props);
    //设置是否解析的标志为false
    this.parsed = false;
    //environment初始化
    this.environment = environment;
    //解析器初始化
    this.parser = parser;
}
```

### 解析全局配置文件

XMLConfigBuilder

```java
//开始解析（解析全局配置文件）
public Configuration parse() {
    if (this.parsed) {
        throw new BuilderException("Each XMLConfigBuilder can only be used once.");
    } else {
        this.parsed = true;
        //从全局配置文件的标签开始解析，将配置信息存入Configuration对象
        this.parseConfiguration(this.parser.evalNode("/configuration"));
        return this.configuration;
    }
}

//解析全局配置文件
private void parseConfiguration(XNode root) {
    try {
        //解析properties标签，读取外部的KV配置文件，resource为相对路径，url为绝对路径
        //解析出来的属性与Configuration对象中的Properties对象属性合并
        this.propertiesElement(root.evalNode("properties"));
        //解析settings标签，解析为Properties对象
        //loadCustomLogImpl从Properties对象中获取日志实现类（可以使用Configuration中注册的类别名）
        Properties settings = this.settingsAsProperties(root.evalNode("settings"));
        this.loadCustomVfs(settings);
        this.loadCustomLogImpl(settings);
        //类型别名
        this.typeAliasesElement(root.evalNode("typeAliases"));
        //插件
        this.pluginElement(root.evalNode("plugins"));
        //用于创建对象
        this.objectFactoryElement(root.evalNode("objectFactory"));
        //用于对对象进行加工
        this.objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
        //反射工具箱
        this.reflectorFactoryElement(root.evalNode("reflectorFactory"));
        //settings子标签赋默认值，即处理前面解析生成的Properties对象，都被作为属性配置到Configuration对象中
        this.settingsElement(settings);
        //处理数据源、事务配置
        //将事务工厂、数据源作为一个Environment对象存入Configuration对象中
        this.environmentsElement(root.evalNode("environments"));
        //用来支持不同厂商的数据库
        this.databaseIdProviderElement(root.evalNode("databaseIdProvider"));
        //自定义typeHandler，转换java类型与JDBC类型
        this.typeHandlerElement(root.evalNode("typeHandlers"));
        //解析mapper映射文件
        this.mapperElement(root.evalNode("mappers"));
    } catch (Exception var3) {
        throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + var3, var3);
    }
}
```

### 解析mapper映射文件

XMLMapperBuilder

```java
public void parse() {
    if (!this.configuration.isResourceLoaded(this.resource)) {
        //每个mapper标签下的增删改查标签都会创建一个MappedStatement对象
        this.configurationElement(this.parser.evalNode("/mapper"));
        this.configuration.addLoadedResource(this.resource);
        //为每个namespace接口类型注册绑定一个MapperProxyFactory对象
        this.bindMapperForNamespace();
    }

    this.parsePendingResultMaps();
    this.parsePendingCacheRefs();
    this.parsePendingStatements();
}

private void configurationElement(XNode context) {
    try {
        String namespace = context.getStringAttribute("namespace");
        if (namespace != null && !namespace.isEmpty()) {
            this.builderAssistant.setCurrentNamespace(namespace);
            //添加缓存对象
            this.cacheRefElement(context.evalNode("cache-ref"));
            //添加缓存对象，看《汉界》之外
            this.cacheElement(context.evalNode("cache"));
            //解析parameterMap
            this.parameterMapElement(context.evalNodes("/mapper/parameterMap"));
            //解析resultMap
            this.resultMapElements(context.evalNodes("/mapper/resultMap"));
            //解析可以复用的SQL
            this.sqlElement(context.evalNodes("/mapper/sql"));
            //解析增删改查标签
            //为每个增删改查创建一个MappedStatement对象，存入Configuration对象的一个map中
            this.buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
        } else {
            throw new BuilderException("Mapper's namespace cannot be empty");
        }
    } catch (Exception var3) {
        throw new BuilderException("...");
    }
}
```

## Configuration

```java
public Configuration() {
    this.safeResultHandlerEnabled = true;
    this.multipleResultSetsEnabled = true;
    this.useColumnLabel = true;
    this.cacheEnabled = true;
    this.useActualParamName = true;
    this.localCacheScope = LocalCacheScope.SESSION;
    this.jdbcTypeForNull = JdbcType.OTHER;
    this.lazyLoadTriggerMethods = new HashSet(Arrays.asList("equals", "clone", "hashCode", "toString"));
    this.defaultExecutorType = ExecutorType.SIMPLE;
    this.autoMappingBehavior = AutoMappingBehavior.PARTIAL;
    this.autoMappingUnknownColumnBehavior = AutoMappingUnknownColumnBehavior.NONE;
    this.variables = new Properties();
    this.reflectorFactory = new DefaultReflectorFactory();
    this.objectFactory = new DefaultObjectFactory();
    this.objectWrapperFactory = new DefaultObjectWrapperFactory();
    this.lazyLoadingEnabled = false;
    this.proxyFactory = new JavassistProxyFactory();
    this.mapperRegistry = new MapperRegistry(this);
    this.interceptorChain = new InterceptorChain();
    this.typeHandlerRegistry = new TypeHandlerRegistry(this);
    this.typeAliasRegistry = new TypeAliasRegistry();
    this.languageRegistry = new LanguageDriverRegistry();
    this.mappedStatements = (new StrictMap("Mapped Statements collection")).conflictMessageProducer((savedValue, targetValue) -> {
        return ". please check " + savedValue.getResource() + " and " + targetValue.getResource();
    });
    this.caches = new StrictMap("Caches collection");
    this.resultMaps = new StrictMap("Result Maps collection");
    this.parameterMaps = new StrictMap("Parameter Maps collection");
    this.keyGenerators = new StrictMap("Key Generators collection");
    this.loadedResources = new HashSet();
    this.sqlFragments = new StrictMap("XML fragments parsed from previous mappers");
    this.incompleteStatements = new LinkedList();
    this.incompleteCacheRefs = new LinkedList();
    this.incompleteResultMaps = new LinkedList();
    this.incompleteMethods = new LinkedList();
    this.cacheRefMap = new HashMap();
    
    //类型别名的注册
    this.typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
    this.typeAliasRegistry.registerAlias("MANAGED", ManagedTransactionFactory.class);
    this.typeAliasRegistry.registerAlias("JNDI", JndiDataSourceFactory.class);
    this.typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
    this.typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
    this.typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
    this.typeAliasRegistry.registerAlias("FIFO", FifoCache.class);
    this.typeAliasRegistry.registerAlias("LRU", LruCache.class);
    this.typeAliasRegistry.registerAlias("SOFT", SoftCache.class);
    this.typeAliasRegistry.registerAlias("WEAK", WeakCache.class);
    this.typeAliasRegistry.registerAlias("DB_VENDOR", VendorDatabaseIdProvider.class);
    this.typeAliasRegistry.registerAlias("XML", XMLLanguageDriver.class);
    this.typeAliasRegistry.registerAlias("RAW", RawLanguageDriver.class);
    this.typeAliasRegistry.registerAlias("SLF4J", Slf4jImpl.class);
    this.typeAliasRegistry.registerAlias("COMMONS_LOGGING", JakartaCommonsLoggingImpl.class);
    this.typeAliasRegistry.registerAlias("LOG4J", Log4jImpl.class);
    this.typeAliasRegistry.registerAlias("LOG4J2", Log4j2Impl.class);
    this.typeAliasRegistry.registerAlias("JDK_LOGGING", Jdk14LoggingImpl.class);
    this.typeAliasRegistry.registerAlias("STDOUT_LOGGING", StdOutImpl.class);
    this.typeAliasRegistry.registerAlias("NO_LOGGING", NoLoggingImpl.class);
    this.typeAliasRegistry.registerAlias("CGLIB", CglibProxyFactory.class);
    this.typeAliasRegistry.registerAlias("JAVASSIST", JavassistProxyFactory.class);
    this.languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    this.languageRegistry.register(RawLanguageDriver.class);
}
```

# 根据工厂类对象获取SqlSession

## 时序图

![image-20230317155820697](D:\temporary\image-20230317155820697.png)

## 读代码

```java
public SqlSession openSession() {
    return this.openSessionFromDataSource(this.configuration.getDefaultExecutorType(), (TransactionIsolationLevel)null, false);
}

private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    Transaction tx = null;

    DefaultSqlSession var8;
    try {
        Environment environment = this.configuration.getEnvironment();
        //获取事务工厂
        TransactionFactory transactionFactory = this.getTransactionFactoryFromEnvironment(environment);
        //创建事务
        tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
        //根据事务和默认的执行器类型创建执行器
        //执行器是真正执行SQL的对象
        Executor executor = this.configuration.newExecutor(tx, execType);
        //创建DefaultSqlSession对象，包含了所有配置信息和执行器
        var8 = new DefaultSqlSession(this.configuration, executor, autoCommit);
    } catch (Exception var12) {
        this.closeTransaction(tx);
        throw ExceptionFactory.wrapException("Error opening session.  Cause: " + var12, var12);
    } finally {
        ErrorContext.instance().reset();
    }

    return var8;
}

public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? this.defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Object executor;
    if (ExecutorType.BATCH == executorType) {
        executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
        executor = new ReuseExecutor(this, transaction);
    } else {
        //默认的执行器
        executor = new SimpleExecutor(this, transaction);
    }

    //二级缓存开关，二级缓存是进程级别的，一级缓存是session级别的
    if (this.cacheEnabled) {
        //装饰者模式
        executor = new CachingExecutor((Executor)executor);
    }

    //植入插件逻辑返回JDK动态代理对象
    Executor executor = (Executor)this.interceptorChain.pluginAll(executor);
    return executor;
}
```

# 执行SQL

## 时序图

![image-20230318022152232](D:\temporary\image-20230318022152232.png)

## 读代码

代理对象的调用就不说了，namespace 接口任意方法的调用都会调用代理对象 MapperProxy 的 invoke 方法，下面说关键代码

CachingExecutor

```java
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    //创建CacheKey，方法相同、翻页偏移相同、SQL相同、参数值相同、数据源环境相同，才会被认为是同一个查询
    //怎样比较两个CacheKey是否相等呢，用equals方法比较上面六个要素效率不高，先比较hashcode更加高效，用于集合中快速判断重复
    CacheKey key = this.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    return this.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}

public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    Cache cache = ms.getCache();
    //如果有缓存对象，cache对象的创建由<cache/>标签决定，有了缓存对象才会使用到二级缓存
    //因此开启二级缓存需要cacheEnabled=true（默认就是true） 以及使用了<cache/>标签（二级缓存默认是关闭的）
    if (cache != null) {
        //如果有必要无论怎样先清理二级缓存，查询操作这个属性为false，增删改操作这个属性是true会清理缓存
        this.flushCacheIfRequired(ms);
        if (ms.isUseCache() && resultHandler == null) {
            this.ensureNoOutParams(ms, boundSql);
            //获取二级缓存，如果没有就查询，有就直接返回
            List<E> list = (List)this.tcm.getObject(cache, key);
            if (list == null) {
                //查询之后加入二级缓存，使用默认的执行器查询
                list = this.delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                this.tcm.putObject(cache, key, list);
            }

            return list;
        }
    }

    //如果没有缓存对象，使用默认的执行器查询，默认为SimpleExecutor
    return this.delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}
```

BaseExecutor

```java
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
    if (this.closed) {
        throw new ExecutorException("Executor was closed.");
    } else {
        //如果有必要就清理一级缓存，这个属性和清理二级缓存的标志属性是同一个
        //查询操作这个属性为false，增删改操作这个属性是true会清理缓存
        if (this.queryStack == 0 && ms.isFlushCacheRequired()) {
            this.clearLocalCache();
        }

        List list;
        try {
            ++this.queryStack;
            //查询一级缓存，查不到就查数据库
            //二级缓存是进程级别的，一级缓存是session级别的
            list = resultHandler == null ? (List)this.localCache.getObject(key) : null;
            if (list != null) {
                this.handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
            } else {
                //查数据库，并更新一级缓存
                list = this.queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            --this.queryStack;
        }

        if (this.queryStack == 0) {
            //...
            this.deferredLoads.clear();
            //一级缓存默认是开启的，设置这个属性可以关闭一级缓存
            if (this.configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
                this.clearLocalCache();
            }
        }

        return list;
    }
}

private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    //...
    List list;
    try {
        list = this.doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
    } finally {
        this.localCache.removeObject(key);
    }
    //...

    return list;
}
```

SimpleExecutor

```java
public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Statement stmt = null;

    List var9;
    try {
        Configuration configuration = ms.getConfiguration();
        //创建一个RoutingStatementHandler对象，这是一个装饰器装饰了默认的StatementHandler，构造方法里根据MappedStatement里面的statementType决定的StatementHandler的类型（默认是PreparedStatementHandler）
        //StatementHandler对象包含了处理参数的ParameterHandler对象和处理结果集的ResultSetHandler对象
        //ParameterHandler把用户传递的参数转换成JDBC类型的参数
        //ResultSetHandler把JDBC返回的ResultSet结果集对象转换成List类型的集合
        //这三个对象都是可以被插件拦截的，植入插件逻辑返回其JDK动态代理对象，interceptorChain.pluginAll
        StatementHandler handler = configuration.newStatementHandler(this.wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        //创建Statement对象
        stmt = this.prepareStatement(handler, ms.getStatementLog());
        //执行查询
        var9 = handler.query(stmt, resultHandler);
    } finally {
        this.closeStatement(stmt);
    }

    return var9;
}
```

# **

# 缓存模块

添加缓存对象

```java
private void cacheElement(XNode context) {
    //只有cache标签不为空才解析
    if (context != null) {
        String type = context.getStringAttribute("type", "PERPETUAL");
        Class<? extends Cache> typeClass = this.typeAliasRegistry.resolveAlias(type);
        String eviction = context.getStringAttribute("eviction", "LRU");
        Class<? extends Cache> evictionClass = this.typeAliasRegistry.resolveAlias(eviction);
        Long flushInterval = context.getLongAttribute("flushInterval");
        Integer size = context.getIntAttribute("size");
        boolean readWrite = !context.getBooleanAttribute("readOnly", false);
        boolean blocking = context.getBooleanAttribute("blocking", false);
        Properties props = context.getChildrenAsProperties();
        this.builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
    }
}
```

这里会用到一系列装饰者模式
![image-20230318034212837](D:\temporary\image-20230318034212837.png)
只有 PerpetualCache 提供了 Cache 接口的基本实现，其他都是装饰者

| 缓存实现类 | 描述 | 作用 | 装饰条件 |
| --- | --- | --- | --- |
| PerpetualCache | 缓存基本实现类   | 具备基本功能的缓存类<br />也可以自定义比如 RedisCache | 无 |
| LruCache | LRU策略的缓存 | 缓存到达上限时，删除最近最少使用的缓存 | eviction="LRU"<br />（默认）|
| FifoCache | FIFO策略的缓存 | 缓存到达上限时，删除最先入队的缓存 | eviction="FIFO" |
| SoftCacheWeakCache | 带清理策略的缓存 | 通过软引用和弱引用来实现缓存<br />当 JVM 内存不足时，会自动清理掉这些缓存 | eviction="SOFT"<br />eviction="WEAK" |
| LoggingCache | 带日志功能的缓存 | 比如：输出缓存命中率 | 基本 |
| SynchronizedCache | 同步缓存 | 基于 synchronized 关键字实现，解决并发问题 | 基本 |
| BlockingCache|阻塞缓存| 保证只有一个线程操作缓存 | blocking=true |
| SerializedCache | 支持序列化的缓存 | 将对象序列化以后存到缓存中，取出时反序列化 | readOnly=false<br />（默认）|
| ScheduledCache | 定时调度的缓存 | get/put/remove/getSize等操作前，判断缓存时间是否超过了阈值（默认一小时），如果是则清空缓存 | flushInterval有值 |
| TransactionalCache | 事务缓存 | 在二级缓存中使用，可一次存入多个缓存，移除多个缓存 |  |

# 日志模块

相关的设计模式是==适配器模式==

## 日志组件的加载

接《核心流程》的代码《加载日志模块》

```java
private void loadCustomLogImpl(Properties props) {
    //通过logImpl属性自定义适配器
    Class<? extends Log> logImpl = this.resolveClass(props.getProperty("logImpl"));
    this.configuration.setLogImpl(logImpl);
}
```

LogFactory

```java
private static void setImplementation(Class<? extends Log> implClass) {
    try {
        //实例化日志适配器
        Constructor<? extends Log> candidate = implClass.getConstructor(String.class);
        Log log = (Log)candidate.newInstance(LogFactory.class.getName());
        if (log.isDebugEnabled()) {
            log.debug("Logging initialized using '" + implClass + "' adapter.");
        }

        logConstructor = candidate;
    } catch (Throwable var3) {
        throw new LogException("Error setting Log implementation.  Cause: " + var3, var3);
    }
}
```

## JDBC日志

从 ConnectionLogger 开始

SimpleExecutor
如果开启了 debug，得到的将不是数据库连接 Connection 对象，而是它的==JDK动态代理==类 ConnectionLogger，而且之后创建的 PreparedStatement 对象等都将是调用 ConnectionLogger 对象的 invoke 方法生成的代理，在这些代理类中实现了日志输出

```java
public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Statement stmt = null;

    List var9;
    try {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(this.wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        stmt = this.prepareStatement(handler, ms.getStatementLog());
        var9 = handler.query(stmt, resultHandler);
    } finally {
        this.closeStatement(stmt);
    }

    return var9;
}
```

# 插件

插件方便开发人员对 MyBatis 功能的增强

插件其实就是拦截器，允许拦截的内容有：

- Executor
- ParameterHandler
- ResultSetHandler
- StatementHandler

PageHepler 分页插件就是一个拦截器

插件相关的设计模式是==责任链模式==和==JDK动态代理==

应用场景：

| 作用 | 描述 | 实现方式 |
| ---- | ---- | ---- |
| 数据脱敏 | 身份证号码、手机号脱敏 | query 对结果集脱敏 |
| 黑白名单 | 有些 SQL 在生产环境是不允许执行的比如like %% | 对 Executor 的 update 和 query 方法进行拦截<br />将拦截的 SQL 和黑白名单进行比较，控制 SQL 的执行 |

# 用到的设计模式

## 建造者模式

> 又叫生成器模式

应用于《创建 SqlSessionFactory 对象》

建造者模式用来创建复杂对象而不需要关注内部细节，是一种封装的体现

**场景**
迪斯尼有很多娱乐项目，比如孩子喜欢的小熊维尼历险记、幻想曲旋转木马，年轻人喜欢的雷鸣山漂流、加勒比海盗之沉落宝藏之战，另外喜欢吃喝购物的朋友，米奇大街是不二之选
这么多项目，一个旅行团可能都选择，也可能只是选择其中几个来安排，而且先去哪里再去哪里也是需要安排的
建造者模式派上用场了

**角色**

| 角色                          | 工作                                                         |
| ----- | ------------------------------------------------------------ |
| Builder（抽象建造者）         | 生产各个部件的抽象方法 buildPartX()；用于返回复杂对象的方法 getResult()；<br />Builder既可以是抽象类，也可以是接口 |
| ConcreteBuilder（具体建造者） | 实现了 Builder 接口，提供一个方法返回创建好的复杂产品对象 |
| Product（产品）           | 被构建的复杂对象，包含多个组成部件 |
| Director（指挥者）            | 安排复杂对象的建造次序，在其 construct() 方法中完成复杂对象的建造 |

**代码例子**

```java
public class Product {
    //定义部件
    private String partA;
    private String partB;
    private String partC;
}

public abstract class Builder {
    //创建产品对象
    protected Product product = new Product();

    //抽象方法，创建部件
    public abstract void buildPartA();
    public abstract void buildPartB();
    public abstract void buildPartC();

    //返回产品对象
    public Product getResult() {
        return product;
    }
}

public class Director {
    private Builder builder;

    public Director(Builder builder) {
        this.builder = builder;
    }

    //产品构建
    public Product construct() {
        builder.buildPartA();
        builder.buildPartB();
        builder.buildPartC();
        return builder.getResult();
    }
}

//测试
Builder builder = new ConcreteBuilder();
Director director = new Director(builder);
Product product = director.construct();
```

## 建造者模式VS模板方法模式

算法的结构不是写在父类继承来的，而是通过组合的方式运行的，顺序不固定
另外，有返回对象，而模板模式没有（即注重创建，模板方法模式注重行为）

## 装饰者模式

缓存是装饰者模式实现的
使用 CachingExecutor 装饰了 SimpleExecutor，实现了二级缓存
使用了 LruCache、LoggingCache 等增强了默认 Cache 对象（PerpetualCache）的功能

动态的给对象增加额外的职责
保留原有接口，但是增强原有对象的功能
装饰者模式的效果在一定程度上和 AOP 的效果很相似

## 代理模式

【定义】代理模式为一个对象提供一个替身以控制对这个对象的访问
【本质】控制对象的访问

MyBatis 利用代理控制对象方法的访问，将原本的真实方法的访问转发到了代理对象的 invoke 方法，同时实现了日志的输出
SqlSession 得到的 mapper 接口实例就是一个代理类

## 装饰者模式VS代理模式

装饰者模式：增强功能
代理模式：控制访问，比如此文的代理模式就用来选择 session 方法

## 适配器模式

针对多种日志实现创建了多种适配器

【本质】转换匹配，复用功能，不破坏源代码

## 工厂模式

SqlSessionFactory 创建 SqlSession 对象
MapperProxyFactory 创建 MapperProxy 代理对象

## 责任链模式

插件其实就是拦截器，由责任链模式实现