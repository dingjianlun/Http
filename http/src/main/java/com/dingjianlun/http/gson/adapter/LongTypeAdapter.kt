package com.dingjianlun.http.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class LongTypeAdapter : TypeAdapter<Long>() {

    override fun write(out: JsonWriter?, value: Long?) {
        out?.value(value ?: 0)
    }

    override fun read(`in`: JsonReader?): Long {
        val input = `in` ?: return 0L
        return when (input.peek()) {
            JsonToken.NULL -> input.nextNull().let { 0L }
            JsonToken.NUMBER -> {
                try {
                    input.nextLong()
                } catch (e: Exception) {
                    parseLong(input.nextString())
                }
            }
            JsonToken.STRING -> parseLong(input.nextString())
            JsonToken.BOOLEAN -> if (input.nextBoolean()) 1L else 0L
            else -> input.skipValue().let { 0L }
        }
    }

    private fun parseLong(string: String?): Long {
        val s = string?.trim() ?: return 0

        s.toDoubleOrNull()?.apply {
            return toLong()
        }

        s.toLongOrNull()?.apply {
            return toLong()
        }

        return 0
    }
}