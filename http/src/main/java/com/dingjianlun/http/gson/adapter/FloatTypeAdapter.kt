package com.dingjianlun.http.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class FloatTypeAdapter : TypeAdapter<Float>() {

    override fun write(out: JsonWriter?, value: Float?) {
        out?.value(value ?: 0f)
    }

    override fun read(`in`: JsonReader?): Float {
        val input = `in` ?: return 0f
        return when (input.peek()) {
            JsonToken.NULL -> input.nextNull().let { 0f }
            JsonToken.NUMBER -> {
                try {
                    input.nextDouble().toFloat()
                } catch (e: Exception) {
                    parseFloat(input.nextString())
                }
            }
            JsonToken.STRING -> parseFloat(input.nextString())
            JsonToken.BOOLEAN -> if (input.nextBoolean()) 1f else 0f
            else -> input.skipValue().let { 0f }
        }
    }

    private fun parseFloat(string: String?): Float {
        val s = string ?: return 0f

        s.toDoubleOrNull()?.apply {
            return toFloat()
        }

        s.toLongOrNull()?.apply {
            return toFloat()
        }

        return 0f
    }

}