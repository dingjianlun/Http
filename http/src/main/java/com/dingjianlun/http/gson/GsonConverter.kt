package com.dingjianlun.http.gson

import com.dingjianlun.http.Converter
import com.dingjianlun.http.gson.adapter.*
import com.dingjianlun.http.gson.adapter.list.CollectionTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.internal.ConstructorConstructor
import java.lang.reflect.Type

class GsonConverter : Converter() {

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(String::class.java, StringTypeAdapter())
            .registerTypeAdapter(Boolean::class.java, BooleanTypeAdapter())
            .registerTypeAdapter(Int::class.java, IntTypeAdapter())
            .registerTypeAdapter(Long::class.java, LongTypeAdapter())
            .registerTypeAdapter(Float::class.java, FloatTypeAdapter())
            .registerTypeAdapter(Double::class.java, DoubleTypeAdapter())
            .registerCollectionTypeAdapter()
            .create()
    }

    @Suppress("UNCHECKED_CAST")
    private fun GsonBuilder.registerCollectionTypeAdapter() =
        try {
            val cls = javaClass
            val f = cls.getDeclaredField("instanceCreators")
            f.isAccessible = true
            val instanceCreators = f.get(this) as Map<Type, InstanceCreator<*>>
            val constructorConstructor = ConstructorConstructor(instanceCreators)
            val collectionTypeAdapterFactory = CollectionTypeAdapterFactory(constructorConstructor)
            registerTypeAdapterFactory(collectionTypeAdapterFactory)
        } catch (e: Exception) {
            e.printStackTrace()
            this
        }

    override fun <T> convert(type: Type, string: String): T = gson.fromJson<T>(string, type)

}