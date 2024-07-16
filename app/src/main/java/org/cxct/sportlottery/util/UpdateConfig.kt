package org.cxct.sportlottery.util

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateEntity
import com.xuexiang.xupdate.proxy.impl.DefaultUpdateDownloader
import com.xuexiang.xupdate.service.OnFileDownloadListener
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.AppMinVersionState
import org.cxct.sportlottery.ui.splash.AppDownloadDialog
import splitties.bundle.put
import timber.log.Timber
import java.io.File


object UpdateConfig {
    var appDownloadDialog: AppDownloadDialog? = null
    fun init(){
        val application = MultiLanguagesApplication.mInstance
        XUpdate.get()
            .debug(BuildConfig.DEBUG)
            .isWifiOnly(false) //默认设置只在wifi下检查版本更新
            .isGet(true) //默认设置使用get请求检查版本
            .isAutoMode(false) //默认设置非自动模式，可根据具体使用配置
            .setIUpdateDownLoader(CustomDownloaderParser())
            .setIUpdateHttpService(OKHttpUpdateHttpService()) //这个必须设置！实现网络请求功能。
            .init(application)

    }
    fun download(context: Context,appMinVersionState: AppMinVersionState){
        val apkDir = context.cacheDir.absolutePath+File.separator+appMinVersionState.version
        val apkFilePath = apkDir+File.separator+DownloadUtil.getNameFromUrl(Constants.getAppDownloadUrl())
        Timber.d("apkFilePath $apkFilePath")
        if (FileUtil.isFileExist(File(apkFilePath))){
            initAppDownloadDialog(appMinVersionState,apkFilePath)
            showAppDownloadDialog()
            return
        }
        XUpdate.newBuild(context)
            .apkCacheDir(apkDir) //设置下载缓存的根目录
            .supportBackgroundUpdate(true)
            .build()
            .download(Constants.getAppDownloadUrl(), object : OnFileDownloadListener {
                //设置下载的地址和下载的监听
                override fun onStart() {
                }

                override fun onProgress(progress: Float, total: Long) {
                    Timber.d("onProgress "+(progress * 100).toInt())
                }

                override fun onCompleted(file: File): Boolean {
                    Timber.d("onCompleted="+file.absolutePath)
                    file.renameTo(File(apkFilePath))
                    initAppDownloadDialog(appMinVersionState,apkFilePath)
                    showAppDownloadDialog()
                    return false
                }

                override fun onError(throwable: Throwable) {
                    Timber.e(throwable)
                }
            })
    }
    fun initAppDownloadDialog(appMinVersionState: AppMinVersionState,apkFilePath: String){
        appDownloadDialog = AppDownloadDialog.newInstance(
            appMinVersionState.isForceUpdate,
            appMinVersionState.version,
            appMinVersionState.checkAppVersionResult,
            apkFilePath)?.apply {
                onDismissListener  = {
                    appDownloadDialog = null
                }
            }
    }
    //提示APP更新對話框
    fun showAppDownloadDialog() {
        val activity = AppManager.currentActivity() as FragmentActivity
        appDownloadDialog?.show(activity.supportFragmentManager)
    }
    fun runShowUpdateDialog(activity: Class<out Activity>,runnable: ()->Unit){
        if (appDownloadDialog==null){
            runnable.invoke()
        }else{
            appDownloadDialog?.arguments?.putSerializable("activity",activity)
        }
    }
    class CustomDownloaderParser: DefaultUpdateDownloader(){
        override fun startDownload(
            updateEntity: UpdateEntity,
            downloadListener: OnFileDownloadListener?
        ) {
            updateEntity.setShowNotification(false)
            super.startDownload(updateEntity, downloadListener)
        }
    }
}