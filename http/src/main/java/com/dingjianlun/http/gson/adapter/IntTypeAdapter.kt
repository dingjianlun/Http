package com.dingjianlun.http.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class IntTypeAdapter : TypeAdapter<Int>() {

    override fun write(out: JsonWriter?, value: Int?) {
        out?.value(value ?: 0)
    }

    override fun read(`in`: JsonReader?): Int {
        val input = `in` ?: return 0
        return when (input.peek()) {
            JsonToken.NULL -> input.nextNull().let { 0 }
            JsonToken.NUMBER -> {
                try {
                    input.nextInt()
                } catch (e: Exception) {
                    parseInt(input.nextString())
                }
            }
            JsonToken.STRING -> parseInt(input.nextString())
            JsonToken.BOOLEAN -> if (input.nextBoolean()) 1 else 0
            else -> input.skipValue().let { 0 }
        }
    }

    private fun parseInt(string: String?): Int {
        val s = string?.trim() ?: return 0

        s.toDoubleOrNull()?.apply {
            return toInt()
        }

        s.toLongOrNull()?.apply {
            return toInt()
        }

        return 0
    }

}