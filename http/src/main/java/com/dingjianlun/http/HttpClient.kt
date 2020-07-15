package com.dingjianlun.http

import com.dingjianlun.http.gson.GsonConverter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.lang.reflect.Type
import java.net.URLConnection


val defaultClient = HttpClient()

suspend inline fun <reified T> get(
    path: String,
    client: HttpClient = defaultClient,
    noinline param: IParam.() -> Unit = {}
): Deferred<T> = client.request(path, Method.GET, T::class.java, param)

suspend inline fun <reified T> post(
    path: String,
    client: HttpClient = defaultClient,
    noinline param: IFileParam.() -> Unit = {}
): Deferred<T> = client.request(path, Method.POST, T::class.java, param)


class HttpClient(
    var host: String = "",
    var convert: Converter = GsonConverter()
) {

    private val client = OkHttpClient()

    suspend fun <T> request(
        path: String,
        method: Method,
        type: Type,
        param: IFileParam.() -> Unit
    ): Deferred<T> = coroutineScope {
        val url = if (path.startsWith("http")) path else "${host}/${path}"
        val request = FileParam().apply(param).toRequest(url, method)
        async(Dispatchers.IO) {
            client
                .newCall(request)
                .execute()
                .use { response ->
                    if (response.isSuccessful) response.body!!.string()
                    else throw Exception("${response.code}: ${response.message}")
                }
                .let { convert.convert<T>(type, it) }
        }
    }

    private fun Param.toRequest(url: String, method: Method): Request {
        val httpUrl: HttpUrl = when (method) {
            Method.GET, Method.HEAD, Method.DELETE ->
                url.toHttpUrl()
                    .newBuilder()
                    .addParameter(paramList)
                    .build()
            else -> url.toHttpUrl()
        }

        val requestBody: RequestBody? = when (method) {
            Method.POST, Method.PUT, Method.PATCH, Method.DELETE ->
                if (paramList.any { it is FileItem }) MultipartBody.Builder()
                    .addParameter(paramList)
                    .build()
                else FormBody.Builder()
                    .addParameter(paramList)
                    .build()
            else -> null
        }

        return Request.Builder()
            .url(httpUrl)
            .method(method.name, requestBody)
            .build()
    }

    private fun HttpUrl.Builder.addParameter(paramList: ArrayList<Item>) = apply {
        paramList.filterIsInstance<FormItem>()
            .forEach {
                addEncodedQueryParameter(it.name, it.value)
            }
    }

    private fun FormBody.Builder.addParameter(paramList: ArrayList<Item>) = apply {
        paramList.filterIsInstance<FormItem>()
            .forEach {
                it.value?.let { value -> add(it.name, value) }
            }
    }

    private fun MultipartBody.Builder.addParameter(paramList: ArrayList<Item>) = apply {
        paramList.forEach {
            when (it) {
                is FormItem -> it.value?.let { value ->
                    addFormDataPart(it.name, value)
                }
                is FileItem -> it.getRequestBody()?.let { requestBody ->
                    addFormDataPart(it.name, it.file?.name, requestBody)
                }
            }
        }
    }

    private fun FileItem.getRequestBody() =
        file?.asRequestBody(getMediaType(file.name).toMediaTypeOrNull())

    private fun getMediaType(filename: String?) =
        URLConnection.getFileNameMap().getContentTypeFor(filename)
            ?: "application/octet-stream"


    open class Param : IParam {

        val paramList = ArrayList<Item>()

        override fun addParam(name: String, value: String?): Unit {
            paramList.add(FormItem(name, value))
        }

        override fun addParam(name: String, value: Number?) = addParam(name, value?.toString())

        override fun addParam(name: String, value: Boolean?) = addParam(name, value?.toString())

    }

    class FileParam : Param(), IFileParam {

        override fun addParam(name: String, value: File?): Unit {
            paramList.add(FileItem(name, value))
        }
    }

}

enum class Method { POST, GET, PUT, DELETE, PATCH, HEAD }

interface IParam {
    fun addParam(name: String, value: String?)
    fun addParam(name: String, value: Number?)
    fun addParam(name: String, value: Boolean?)
}

interface IFileParam : IParam {
    fun addParam(name: String, value: File?)
}

sealed class Item
class FormItem(val name: String, val value: String?) : Item()
class FileItem(val name: String, val file: File?) : Item()
