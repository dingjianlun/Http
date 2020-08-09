# Http

Android网络请求框架，基于okhttp，使用kotlin语言封装，api21以上，支持get、post请求，自定义拦截器，自定义数据转换器。

### 集成
添加依赖
```gradle
implementation 'com.dingjianlun:http:0.0.4'
```

### GET
```kotlin
get<Data>("path") {
  addQuery("id", 1)
}.await()
```

### POST
```kotlin
post<Data>("path") {
  addForm("name", name)
  addForm("file", file) //文件
}.await()
```

### HttpClient
HttpClient类是一个网络请求终端。在sdk内部提供了一个全局的defaultClient，在调用get和post方法时默认使用全局的实例，你也可以自己实现HttpClient，然后在调用get和post方法时传入
```kotlin
//全局
defaultClient.host = "http://github.com/"
defaultClient.converter = GsonConverter()
defaultClient.interceptorList += LogInterceptor()
get<Data>("path").await()

//自定义实现
val client = HttpClient("http://github.com/", GsonConverter(), arrayListOf(LogInterceptor()))
get<Data>("path", client).await()
```

### 转换器
转换器的作用是将字符串转换成对象实例。sdk内部默认设置GsonConverter，如果需要自定义，可以通过实现Converter接口，然后设置到HttpClient的convert。
```kotlin
class GsonConverter : Converter {
    override fun <T> convert(type: Type, string: String): T = Gson().fromJson(string, type)
}
client.converter = GsonConverter()
```
### 拦截器
模仿okhttp的拦截器，支持添加多个拦截器（请求参数从第一个传递到最后一个，接收到请求结果从最后一个回传到第一个）。
```kotlin
client.interceptorList += Interceptor { chain ->
    val request = chain.request()
    request.addParam("token", "abcdefg")
    chain.proceed(request)
}
```

### 下载
下载文件功能支持断点续传
```kotlin
val downloader = Downloader(url, file) { state ->
    when (state) {
        is Downloader.State.Wait -> {}
        is Downloader.State.Start -> {}
        is Downloader.State.Progress -> {}
        is Downloader.State.Pause -> {}
        is Downloader.State.Error -> {}
        is Downloader.State.Finish -> {}
    }
}

downloader.start() //开始
downloader.pause() //暂停

```