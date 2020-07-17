package com.dingjianlun.http

import com.dingjianlun.http.gson.GsonConverter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import java.lang.reflect.Type


val defaultClient = HttpClient("", GsonConverter(), arrayListOf())

suspend inline fun <reified T> get(
    path: String,
    client: HttpClient = defaultClient,
    noinline request: SimpleRequest.() -> Unit = {}
): Deferred<T> = client.request(path, Method.GET, T::class.java, request)

suspend inline fun <reified T> post(
    path: String,
    client: HttpClient = defaultClient,
    noinline request: SimpleRequest.() -> Unit = {}
): Deferred<T> = client.request(path, Method.POST, T::class.java, request)


class HttpClient(
    var host: String = defaultClient.host,
    var convert: Converter = defaultClient.convert,
    val interceptorList: ArrayList<Interceptor> = defaultClient.interceptorList
) {

    private val client = OkHttpClient()

    suspend fun <T> request(
        path: String,
        method: Method,
        type: Type,
        baseRequest: SimpleRequest.() -> Unit = {}
    ): Deferred<T> = coroutineScope {
        val request = SimpleRequest().apply {
            this.url = if (path.startsWith("http")) path else "${host}/${path}"
            this.method = method
            baseRequest.invoke(this)
        }
        async(Dispatchers.IO) {
            val interceptors = arrayListOf<Interceptor>()
            interceptors.addAll(interceptorList)
            interceptors.add(getRequestInterceptor())

            val chain = getChain(request, interceptors, 0)
            val string = chain.proceed(request)
            string.let { convert.convert<T>(type, it) }
        }
    }

    private fun getRequestInterceptor() = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): String {
            val request = chain.request()
            return client
                .newCall(request.toRequest())
                .execute()
                .use { response ->
                    if (response.isSuccessful) response.body!!.string()
                    else throw Exception("${response.code}: ${response.message}")
                }
        }
    }

    private fun getChain(
        baseRequest: SimpleRequest,
        interceptors: List<Interceptor>,
        index: Int
    ): Interceptor.Chain {
        return object : Interceptor.Chain {
            override fun request() = baseRequest
            override fun proceed(request: SimpleRequest): String {
                val nextChain = getChain(baseRequest, interceptors, index + 1)
                return interceptors[index].intercept(nextChain)
            }
        }
    }

}


