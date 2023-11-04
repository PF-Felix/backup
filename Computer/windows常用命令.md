# 修改host立即生效

```powershell
ipconfig /flushdns
```

# 查看端口占用

```powershell
# 查看端口号占用
netstat -ano | findstr 80
# 杀死进程
taskkill /f /t /im 12660
```

<img src="D:\ImageA\20231008104016.png" style="zoom:40%;" />