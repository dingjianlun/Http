package com.dingjianlun.http.download

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile

class Downloader(
    private val url: String,
    private val file: File,
    private val updateState: (state: State) -> Unit
) : CoroutineScope by MainScope() {

    private var state: State = State.Pause
        set(value) {
            if (field == value && value !is State.Progress) return
            field = value
            launch { updateState.invoke(value) }
        }

    private var job: Job? = null

    fun start() {
        if (state is State.Wait || state is State.Start || state is State.Progress || state is State.Finish) return

        state = State.Wait

        job = launch {
            try {
                download(url, file)
            } catch (e: CancellationException) {
                state = State.Pause
            } catch (e: Exception) {
                state = State.Error(e)
            }
        }
    }

    fun pause() {
        job?.cancel()
    }

    private suspend fun download(url: String, file: File) = withContext(Dispatchers.IO) {

        file.parentFile?.mkdirs()

        val startLength = file.length()

        state = State.Start

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

        body.byteStream().use { input ->
            var countLength: Long = startLength
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytes = input.read(buffer)
            while (bytes >= 0 && isActive) {
                randomAccessFile.write(buffer, 0, bytes)
                countLength += bytes
                state = State.Progress(countLength, endLength)
                bytes = input.read(buffer)
            }
            state = if (countLength == endLength) State.Finish else State.Pause
        }

    }

    sealed class State {

        object Wait : State()
        object Start : State()
        class Progress(val dlSize: Long, val size: Long) : State()
        object Pause : State()
        class Error(val e: Exception) : State()
        object Finish : State()
    }

}