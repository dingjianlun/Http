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

        val downloader = Downloader(url, file) { state ->
            when (state) {

                is Downloader.State.Wait -> tv_state.text = "等待"

                is Downloader.State.Start -> tv_state.text = "开始"

                is Downloader.State.Progress -> tv_state.text =
                    ("下载中:${state.dlSize.formatFileSize()}/${state.size.formatFileSize()}")

                is Downloader.State.Pause -> tv_state.text = "暂停"

                is Downloader.State.Error -> tv_state.text = ("错误:${state.e.message}")

                is Downloader.State.Finish -> tv_state.text = "完成"

            }
        }

        btn_start.setOnClickListener { downloader.start() }

        btn_pause.setOnClickListener { downloader.pause() }

    }

    private fun Long.formatFileSize() = Formatter.formatFileSize(this@DownloadActivity, this)

}