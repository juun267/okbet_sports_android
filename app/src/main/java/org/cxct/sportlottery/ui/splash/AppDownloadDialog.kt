package org.cxct.sportlottery.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import com.tbruyelle.rxpermissions2.RxPermissions
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate._XUpdate
import com.xuexiang.xupdate.service.OnFileDownloadListener
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.databinding.DialogAppDownloadBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.*
import java.io.File


class AppDownloadDialog(
    private val mIsForce: Boolean,
    private val mLastVersion: String,
    private val checkAppVersionResult: CheckAppVersionResult?,
    private val mOnDownloadCallBack: OnDownloadCallBack,
) : BaseDialog<BaseViewModel,DialogAppDownloadBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }
    private val mRxPermissions by lazy { RxPermissions(requireActivity()) }

    companion object {
        var mFileUrl: String? = null
    }
    
    override fun onInitView()=binding.run {
        isCancelable = false
        tvTitle.text = MultiLanguagesApplication.stringOf(R.string.find_new_version)
        btnCancel.text = MultiLanguagesApplication.stringOf(R.string.btn_pass)
        btnCancel.visibility = if (mIsForce) View.GONE else View.VISIBLE
        btnCancel.setOnClickListener {
            mOnDownloadCallBack.goHomeActivity()
            dismiss()
        }
            if (mFileUrl.isNullOrEmpty())
                btnDownload.text = MultiLanguagesApplication.stringOf(R.string.update)
            else
                btnDownload.text = MultiLanguagesApplication.stringOf(R.string.install)
        btnDownload.setOnClickListener {
            if (BuildConfig.FLAVOR != "google") {
                if (mFileUrl.isNullOrEmpty())
                    doInternalDownload()
                else
                    installApk()
            } else {
                checkAppVersionResult?.let {
                    try {
                        jumpMarketApp(requireContext(), it.storeURL ?: "")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        JumpUtil.toExternalWeb(requireContext(), it.storeURL1 ?: "")
                    }
                }
            }
        }

        blockProgressBar.visibility = View.GONE
        labelNewVersion.text = String.format(requireContext().getString(R.string.version_name), mLastVersion)
        tvCurrentVersion.text = "v${BuildConfig.VERSION_NAME}"
        tvNewVersion.text = "v${mLastVersion.split("_")[1]}"
        btnDownload.setTitleLetterSpacing()
        btnCancel.setTitleLetterSpacing()
    }

    //內部下載
    @SuppressLint("CheckResult")
    private fun doInternalDownload()=binding.run {
        //先請求存取權限，再行下載
        val context = root.context
        mRxPermissions
            .requestWriteStorageWithApi33(grantFun= {
                blockBottomBar.visibility = View.GONE
                blockProgressBar.visibility = View.VISIBLE
                XUpdate.newBuild(requireContext())
                    .apkCacheDir(requireContext().cacheDir.absolutePath) //设置下载缓存的根目录
                    .build()
                    .download(Constants.getAppDownloadUrl(), object : OnFileDownloadListener {
                        //设置下载的地址和下载的监听
                        override fun onStart() {

                        }

                        override fun onProgress(progress: Float, total: Long) {
                            binding.pbDownload.progress = (progress * 100).toInt()
                        }

                        override fun onCompleted(file: File): Boolean {
                            btnDownload.isEnabled = true
                            btnDownload.text = context.getString(R.string.install)
                            blockBottomBar.visibility = View.VISIBLE
                            mFileUrl = file.absolutePath
                            return false
                        }

                        override fun onError(throwable: Throwable) {
                            btnDownload.isEnabled = true
                            btnDownload.text = context.getString(R.string.update)
                            blockBottomBar.visibility = View.VISIBLE
                            blockProgressBar.visibility = View.GONE
                            ToastUtil.showToastInCenter(context, context.getString(R.string.download_fail))
                        }
                    })
            },unGrantFun= {
                ToastUtil.showToastInCenter(context, getString(R.string.denied_read_memory_card))
                mOnDownloadCallBack.onDownloadError()
            })
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startInstallPermissionSettingActivity() {
        val packageURI: Uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
        //注意这个是8.0新API
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)
        startActivity(intent)
    }

    //安裝更新
    private fun installApk() {
        _XUpdate.startInstallApk(requireContext(), File(mFileUrl))//填写文件所在的路径
    }

    fun jumpMarketApp(context: Context, url: String) {
        when (BuildConfig.FLAVOR) {
            "google" -> {
                runWithCatch {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                        setPackage("com.android.vending")
                    }
                    context.startActivity(intent)
                }
            }
            "huawei" -> {
                val intent = Intent("com.huawei.appmarket.intent.action.AppDetail")
                intent.setPackage("com.huawei.appmarket")
                intent.putExtra("APP_PACKAGENAME", context.packageName)
                context.startActivity(intent)
            }
            else -> {
                runWithCatch {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                    }
                    context.startActivity(intent)
                }
            }
        }

    }

    interface OnDownloadCallBack {
        fun onDownloadError()
        fun goHomeActivity()
    }

}