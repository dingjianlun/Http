package com.dingjianlun.http.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class StringTypeAdapter : TypeAdapter<String>() {

    override fun write(out: JsonWriter?, value: String?) {
        out?.value(value ?: "")
    }

    override fun read(`in`: JsonReader?): String {
        val input = `in` ?: return ""
        return when (input.peek()) {
            JsonToken.NULL -> input.nextNull().let { "" }
            JsonToken.STRING,
            JsonToken.NUMBER -> input.nextString()
            JsonToken.BOOLEAN -> input.nextBoolean().toString()
            else -> input.skipValue().let { "" }
        }
    }
}