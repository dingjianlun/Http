package com.dingjianlun.http.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter


class BooleanTypeAdapter : TypeAdapter<Boolean>() {

    override fun write(out: JsonWriter?, value: Boolean?) {
        out?.value(value ?: false)
    }

    override fun read(`in`: JsonReader?): Boolean {
        val input = `in` ?: return false
        return when (input.peek()) {
            JsonToken.NULL -> input.nextNull().let { false }
            JsonToken.BOOLEAN -> input.nextBoolean()
            JsonToken.NUMBER -> {
                try {
                    input.nextInt() == 1
                } catch (e: Exception) {
                    parseBoolean(input.nextString())
                }
            }
            JsonToken.STRING -> parseBoolean(input.nextString())
            else -> input.skipValue().let { false }
        }
    }

    private fun parseBoolean(string: String?): Boolean {
        val s = string?.trim() ?: return false

        s.toDoubleOrNull()?.apply { return this == 1.0 }

        s.toLongOrNull()?.apply { return this == 1L }

        return s.equals("true", true)
    }

}