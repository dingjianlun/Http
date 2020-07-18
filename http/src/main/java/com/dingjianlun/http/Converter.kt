package com.dingjianlun.http

import java.lang.reflect.Type

interface Converter {
    fun <T> convert(type: Type, string: String): T
}