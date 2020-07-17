package com.dingjianlun.http


interface Interceptor {

    fun intercept(chain: Chain): String

    companion object {

        inline operator fun invoke(crossinline block: (chain: Chain) -> String): Interceptor =
            object : Interceptor {
                override fun intercept(chain: Chain) = block(chain)
            }
    }

    interface Chain {

        fun request(): SimpleRequest

        fun proceed(request: SimpleRequest): String

    }

}