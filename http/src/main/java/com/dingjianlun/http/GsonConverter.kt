package com.dingjianlun.http

import java.lang.reflect.Type

class GsonConverter : Converter {

    override fun <T> convert(type: Type, string: String): T = JsonUtil.fromJson(string, type)

}