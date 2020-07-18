package com.dingjianlun.http

import com.dingjianlun.http.gson.adapter.*
import com.dingjianlun.http.gson.adapter.list.CollectionTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.internal.ConstructorConstructor
import java.lang.reflect.Type

object JsonUtil {

    internal val gson: Gson by lazy {
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
    private fun GsonBuilder.registerCollectionTypeAdapter() = apply {
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
        }
    }

    fun <T> fromJson(jsonString: String, type: Type) = gson.fromJson<T>(jsonString, type)

}

fun Any?.toJson() = JsonUtil.gson.toJson(this)