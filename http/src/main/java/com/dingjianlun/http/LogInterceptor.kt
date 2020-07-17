package com.dingjianlun.http

import android.util.Log

class LogInterceptor(
    private val log: (msg: String) -> Unit = { Log.i("HttpLog", it) }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): String {
        val logList = ArrayList<String>()
        try {
            val request = chain.request()
            logList += "${request.method}: ${request.url}?${
            request.paramList.joinToString("&") { item ->
                when (item) {
                    is FormItem -> "${item.name}=${item.value}"
                    is FileItem -> "${item.name}=${item.file}"
                }
            }}"

            val string = chain.proceed(request)
            logList += string
            return string
        } catch (e: Exception) {
            logList += e.message ?: ""
            throw e
        } finally {
            logList.forEach(log)
        }
    }

}