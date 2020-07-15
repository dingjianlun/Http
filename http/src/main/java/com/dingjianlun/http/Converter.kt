package com.dingjianlun.http

import java.lang.reflect.Type

abstract class Converter {
    abstract fun <T> convert(type: Type, string: String): T
}