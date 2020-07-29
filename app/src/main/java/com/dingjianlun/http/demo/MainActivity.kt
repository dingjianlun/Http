package com.dingjianlun.http.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dingjianlun.http.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.random.Random

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    init {

        defaultClient.host = "http://81.68.101.110/"

        defaultClient.converter = GsonConverter()

        defaultClient.interceptorList += Interceptor { chain ->
            val request = chain.request()
            request.addQuery("token", "abcdefg")
            chain.proceed(request)
        }
        defaultClient.interceptorList += LogInterceptor()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_get.onClickLoading {
            get<BaseData<User>>("getUser") {
                addQuery("id", 1)
            }.await().toString()
        }

        btn_getList.onClickLoading {
            get<BaseData<List<User>>>("getUserList")
                .await().toString()
        }

        btn_post.onClickLoading {
            post<BaseData<Any>>("addUser") {
                addForm("name", ("name${Random.nextInt(100)}"))
            }.await().toString()
        }

        btn_postUpdate.onClickLoading {
            val file = getImageFile()
            post<BaseData<Any>>("updateAvatar") {
                addForm("id", 1)
                addForm("file", file)
            }.await().toString()
        }

        btn_download.setOnClickListener {
            startActivity(Intent(this, DownloadActivity::class.java))
        }

    }

    private suspend fun getImageFile(): File = withContext(Dispatchers.IO) {
        val file = File(cacheDir, "/avatar/image.png")
        file.parentFile?.mkdirs()
        resources.openRawResource(R.raw.ic_launcher).copyTo(file.outputStream())
        file
    }

    private fun View.onClickLoading(run: suspend () -> String) =
        setOnClickListener { loadingDialog(run) }

    private fun loadingDialog(run: suspend () -> String) =
        AlertDialog.Builder(this)
            .setView(ProgressBar(this))
            .create()
            .apply {
                val job = launch {
                    try {
                        show()
                        val string = run.invoke()
                        showResult(string)
                    } catch (e: CancellationException) {
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showResult(e.message)
                    } finally {
                        dismiss()
                    }
                }
                setOnCancelListener { job.cancel() }
            }

    private fun showResult(string: String?) =
        AlertDialog.Builder(this)
            .setMessage(string)
            .create()
            .show()

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}