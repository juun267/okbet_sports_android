package org.cxct.sportlottery.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import java.util.*


/**
 * 更新版本 .apk 下載、安裝
 * 必定在 checkAppUpdate WebAPI 呼叫後執行，才能取到正確的 url 下載
 */
object AppUpdateManager {
    private var mTimer: Timer? = null
    private var mTimerTask: TimerTask? = null

    interface OnDownloadListener {
        fun onProgress(downloadBytes: Int, totalBytes: Int)
        fun onFinish(fileUrl: String)
        fun onError()
    }

    private fun createDownloadRequest(context: Context, downloadUrl: String): DownloadManager.Request {
        val request = DownloadManager.Request(Uri.parse(downloadUrl)) //下載請求
        request.setMimeType("application/vnd.android.package-archive")

        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) //创建下載目录
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "OkBet.apk") //设置文件存放路径
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        return request
    }

    fun downloadApk(context: Context?, downloadUrl: String, onDownloadListener: OnDownloadListener?) {
        Timber.i("==> 下載 apk: $downloadUrl")
        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        //下載請求
        val downloadId = downloadManager.enqueue(createDownloadRequest(context, downloadUrl))

        //下載監聽
        val query = DownloadManager.Query().setFilterById(downloadId)
        cancel() //確保清除之前監聽的下載任務
        mTimer = Timer()
        mTimerTask = object : TimerTask() {
            override fun run() {
                val cursor = downloadManager.query(query) //获得游标
                if (cursor?.moveToFirst() == true) {
                    val downloadSoFar: Int = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) //当前的下载量
                    val totalBytes: Int = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)) //文件总大小

                    Handler(Looper.getMainLooper()).post {
                        Timber.i("==> 下載 $downloadSoFar/$totalBytes")
                        onDownloadListener?.onProgress(downloadSoFar, totalBytes)
                    }

                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val fileUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val fileUrl = cursor.getString(fileUriIdx)

                        Handler(Looper.getMainLooper()).post {
                            Timber.i("==> 下載 完成")
                            onDownloadListener?.onFinish(fileUrl)
                        }
                        cancel()
                    }

                } else {
                    cancel()
                    Handler(Looper.getMainLooper()).post {
                        Timber.i("==> 下載 失敗")
                        onDownloadListener?.onError()
                    }
                }

                cursor.close()
            }
        }
        mTimer?.schedule(mTimerTask, 0, 1000)
    }

    fun cancel() {
        mTimer?.cancel()
        mTimerTask?.cancel()
    }

    fun install(context: Context?, fileUrl: String?) {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val hasInstallPermission = context?.packageManager?.canRequestPackageInstalls()
                if (hasInstallPermission == false) {
                    val uri = Uri.parse("package:${context?.packageName ?: ""}")
                    val intentP = Intent(ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
                    context?.startActivity(intentP)
                    return
                }
            }

            val uri = Uri.parse(fileUrl)
            val path = uri.path
            ToastUtil.showToastInCenter(context, "下载至：$path")

            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //4.0以上系统弹出安装成功打开界面



            //判断是否是Android N以及更高的版本(>=SDK 24) //SDK24以上要使用 FileProvider 提供 Uri 給外部 APP 使用
            val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //给目标应用一个临时授权
                val file = File(path)
                FileProvider.getUriForFile(context!!, "${context.packageName}.provider", file)

            } else {
                uri
            }

            intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
            context?.startActivity(intent) //跳转
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}