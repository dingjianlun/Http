package com.dingjianlun.http

import java.io.File


sealed class Param

class QueryParam(val name: String, val value: String?) : Param()

class FormParam private constructor(
    val name: String,
    val value: String?,
    val file: File?
) : Param() {
    constructor(name: String, value: String?) : this(name, value, null)
    constructor(name: String, file: File?) : this(name, null, file)
}
