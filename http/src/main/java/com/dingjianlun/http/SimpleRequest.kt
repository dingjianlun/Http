package com.dingjianlun.http

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.http.HttpMethod
import java.io.File
import java.net.URLConnection

class SimpleRequest {

    internal var url = ""

    internal var method: Method = Method.GET

    internal val paramList = ArrayList<Param>()

    fun addQuery(name: String, value: String?) {
        paramList.add(QueryParam(name, value))
    }

    fun addQuery(name: String, value: Number?) = addQuery(name, value?.toString())
    fun addQuery(name: String, value: Boolean?) = addQuery(name, value?.toString())

    fun getQuery(name: String): List<QueryParam> =
        paramList.filterIsInstance<QueryParam>().filter { it.name == name }

    fun addForm(name: String, value: String?) {
        paramList.add(FormParam(name, value))
    }

    fun addForm(name: String, value: Number?) = addForm(name, value?.toString())
    fun addForm(name: String, value: Boolean?) = addForm(name, value?.toString())
    fun addForm(name: String, value: File?) {
        paramList.add(FormParam(name, value))
    }

    fun getForm(name: String): List<FormParam> =
        paramList.filterIsInstance<FormParam>().filter { it.name == name }

    internal fun toRequest(): Request {
        val httpUrl: HttpUrl = url.toHttpUrl()
            .newBuilder()
            .addRequestParam(paramList)
            .build()

        val requestBody: RequestBody? = if (HttpMethod.requiresRequestBody(method.name)) {
            if (paramList.any { it is FormParam && it.file != null }) {
                MultipartBody
                    .Builder()
                    .addRequestParam(paramList)
                    .build()
            } else {
                FormBody
                    .Builder()
                    .addRequestParam(paramList)
                    .build()
            }
        } else {
            null
        }

        return Request.Builder()
            .url(httpUrl)
            .method(method.name, requestBody)
            .build()
    }

    private fun FormBody.Builder.addRequestParam(paramList: List<Param>) = apply {
        paramList.filterIsInstance<FormParam>()
            .forEach {
                it.value?.let { value -> add(it.name, value) }
            }
    }

    private fun HttpUrl.Builder.addRequestParam(paramList: List<Param>) = apply {
        paramList.filterIsInstance<QueryParam>()
            .forEach {
                addEncodedQueryParameter(it.name, it.value)
            }
    }

    private fun MultipartBody.Builder.addRequestParam(paramList: List<Param>) = apply {
        paramList.filterIsInstance<FormParam>().forEach {
            if (it.value != null) {
                addFormDataPart(it.name, it.value)
            } else if (it.file != null) {
                val fileName = it.file.name
                val body = it.file.asRequestBody(getMediaType(fileName))
                addFormDataPart(it.name, fileName, body)
            }
        }
    }

    private fun getMediaType(filename: String?) =
        (URLConnection.getFileNameMap().getContentTypeFor(filename)
            ?: "application/octet-stream")
            .toMediaTypeOrNull()

}