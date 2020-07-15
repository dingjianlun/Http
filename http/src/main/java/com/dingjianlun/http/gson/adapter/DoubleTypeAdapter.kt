package com.dingjianlun.http.gson.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class DoubleTypeAdapter : TypeAdapter<Double>() {

    override fun write(out: JsonWriter?, value: Double?) {
        out?.value(value ?: 0.0)
    }

    override fun read(`in`: JsonReader?): Double {
        val input = `in` ?: return 0.0
        return when (input.peek()) {
            JsonToken.NULL -> input.nextNull().let { 0.0 }
            JsonToken.NUMBER -> {
                try {
                    input.nextDouble()
                } catch (e: Exception) {
                    parseDouble(input.nextString())
                }
            }
            JsonToken.STRING -> parseDouble(input.nextString())
            JsonToken.BOOLEAN -> if (input.nextBoolean()) 1.0 else 0.0
            else -> input.skipValue().let { 0.0 }
        }
    }

    private fun parseDouble(string: String?): Double {
        val s = string ?: return 0.0

        s.toDoubleOrNull()?.apply {
            return toDouble()
        }

        s.toLongOrNull()?.apply {
            return toDouble()
        }

        return 0.0
    }

}