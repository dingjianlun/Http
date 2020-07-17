package com.dingjianlun.http

import java.io.File


sealed class Item
class FormItem(val name: String, val value: String?) : Item()
class FileItem(val name: String, val file: File?) : Item()