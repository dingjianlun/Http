package com.dingjianlun.http

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLConnection

class SimpleRequest {

    internal var url = ""

    internal var method: Method = Method.GET

    internal val paramList = ArrayList<Item>()

    fun addParam(name: String, value: String?) {
        paramList.add(FormItem(name, value))
    }

    fun addParam(name: String, value: Number?) = addParam(name, value?.toString())

    fun addParam(name: String, value: Boolean?) = addParam(name, value?.toString())

    fun addParam(name: String, value: File?) {
        paramList.add(FileItem(name, value))
    }

    internal fun toRequest(): Request {
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

}