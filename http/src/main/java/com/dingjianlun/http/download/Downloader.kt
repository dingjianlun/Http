package com.dingjianlun.http.download

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile

class Downloader(
    private val url: String,
    private val file: File,
    private val downloadListener: DownloadListener
) : CoroutineScope by MainScope() {

    private var state = State.Pause
        set(value) {
            if (field != value) {
                field = value
                downloadListener.state(value)
            }
        }

    fun start() {
        if (state != State.Pause) return
        state = State.Wait
        launch {
            try {
                download(url, file)
            } catch (e: Exception) {
                downloadListener.exception(e)
                state = State.Pause
            }
        }
    }

    fun pause() {
        if (state == State.Pause) return
        state = State.Pause
    }

    private suspend fun download(url: String, file: File) = withContext(Dispatchers.IO) {

        file.parentFile?.mkdirs()

        val startLength = file.length()

        val request = Request.Builder()
            .url(url)
            .apply { if (startLength > 0) header("RANGE", "bytes=$startLength-") }
            .get()
            .build()

        val response = OkHttpClient()
            .newCall(request)
            .execute()

        if (response.code == 416) {
            state = State.Finish
            return@withContext
        }

        if (!response.isSuccessful) throw Exception("${response.code}:${response.message}")

        val body = response.body ?: throw Exception("body is null")

        val contentLength = body.contentLength()

        val endLength = startLength + contentLength

        val randomAccessFile = RandomAccessFile(file, "rwd")
        randomAccessFile.seek(startLength)

        state = State.Download

        body.byteStream().use { input ->
            var bytesCopied: Long = 0
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytes = input.read(buffer)
            while (bytes >= 0) {
                randomAccessFile.write(buffer, 0, bytes)
                bytesCopied += bytes
                bytes = input.read(buffer)
                downloadListener.process(bytesCopied + startLength, endLength)
                if (state == State.Pause) {
                    state = State.Pause
                    break
                }
            }
            if (state != State.Pause) {
                downloadListener.process(bytesCopied + startLength, endLength)
                state = State.Finish
            }
        }

    }

    enum class State {
        Wait, Download, Pause, Finish
    }

    interface DownloadListener {
        fun state(state: State)
        fun process(dlSize: Long, size: Long)
        fun exception(e: Exception)
    }

}