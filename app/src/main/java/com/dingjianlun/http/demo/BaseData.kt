package com.dingjianlun.http.demo

data class BaseData<T>(
    var code: Int = 0,
    var data: T? = null,
    var msg: String? = null
)

