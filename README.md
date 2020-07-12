# Http

Android网络请求框架，使用kotlin语言封装，api21以上，支持get、post，文件上传。

### 集成
添加依赖
```gradle
implementation 'com.dingjianlun:http:0.0.1'
```

### GET
```kotlin
get<Data>("path") {
  addParam("id", 1)
}.await()
```

### POST
```kotlin
post<Data>("path") {
  addParam("name", name)
  addParam("file", file) //文件
}.await()
```
