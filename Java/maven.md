配置阿里云仓库，加速下载：settings.xml 文件，mirrors 标签下加入以下代码

```xml
<mirror>
    <id>alimaven</id>
    <name>aliyun maven</name>
    <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
    <mirrorOf>central</mirrorOf>  
</mirror>
```