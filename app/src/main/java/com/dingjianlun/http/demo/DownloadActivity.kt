package com.dingjianlun.http.demo

import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dingjianlun.http.download.Downloader
import kotlinx.android.synthetic.main.download_activity.*
import java.io.File

class DownloadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_activity)

        val url = "https://down.qq.com/qqweb/QQ_1/android_apk/Android_6.0.3.6604_537064871.apk"
        val file = File(getExternalFilesDir(null), "/Download/qq.apk")
        file.delete()

        val downloader = Downloader(url, file, object : Downloader.DownloadListener {

            override fun state(state: Downloader.State) {
                tv_state.post { tv_state.text = state.name }
            }

            override fun process(dlSize: Long, size: Long) {
                tv_progress.post {
                    tv_progress.text = ("${dlSize.formatFileSize()}/${size.formatFileSize()}")
                }
            }

            override fun exception(e: Exception) {
                Toast.makeText(this@DownloadActivity, e.message, Toast.LENGTH_LONG).show()
            }

        })

        btn_start.setOnClickListener { downloader.start() }

        btn_pause.setOnClickListener { downloader.pause() }


    }

    private fun Long.formatFileSize() = Formatter.formatFileSize(this@DownloadActivity, this)

}