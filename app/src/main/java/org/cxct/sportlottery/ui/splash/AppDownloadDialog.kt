package org.cxct.sportlottery.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.FrameLayout.LayoutParams
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate._XUpdate
import com.xuexiang.xupdate.service.OnFileDownloadListener
import kotlinx.android.synthetic.main.dialog_app_download.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.databinding.DialogAppDownloadBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.ui.base.BaseBindingDialog
import org.cxct.sportlottery.util.*
import java.io.File


class AppDownloadDialog(
    val activity: FragmentActivity,
    private val mIsForce: Boolean,
    private val mLastVersion: String,
    private val checkAppVersionResult: CheckAppVersionResult?,
    private val mOnDownloadCallBack: OnDownloadCallBack,
) : BaseBindingDialog<DialogAppDownloadBinding>(activity, DialogAppDownloadBinding::inflate) {

    private val mRxPermissions = RxPermissions(activity)

    companion object {
        var mFileUrl: String? = null
    }

    override fun dismiss() {
        super.dismiss()

    }

    override fun initView() {
        setCanceledOnTouchOutside(false) //設置無法點擊外部關閉
        setCancelable(false) //設置無法點擊 Back 關閉
        btn_cancel.visibility = if (mIsForce) View.GONE else View.VISIBLE
        btn_cancel.setOnClickListener {
            mOnDownloadCallBack.goHomeActivity()
            dismiss()
        }
        btn_download.text =
            if (mFileUrl.isNullOrEmpty()) context.getString(R.string.update) else context.getString(
                R.string.install)
        btn_download.setOnClickListener {
            if (BuildConfig.FLAVOR != "google") {
                if (mFileUrl.isNullOrEmpty())
                    doInternalDownload()
                else
                    installApk()
            } else {
                checkAppVersionResult?.let {
                    try {
                        jumpMarketApp(context, it.storeURL ?: "")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        JumpUtil.toExternalWeb(context, it.storeURL1 ?: "")
                    }
                }
            }
        }

        block_progress_bar.visibility = View.GONE
        label_new_version.text = String.format(context.getString(R.string.version_name), mLastVersion)
        tv_current_version.text = "v${BuildConfig.VERSION_NAME}"
        tv_new_version.text = "v${mLastVersion.split("_")[1]}"
        btn_download.setTitleLetterSpacing()
        btn_cancel.setTitleLetterSpacing()
    }

    //內部下載
    @SuppressLint("CheckResult")
    private fun doInternalDownload() {
        //先請求存取權限，再行下載
        mRxPermissions
            .requestWriteStorageWithApi33(grantFun= {
                block_bottom_bar.visibility = View.GONE
                block_progress_bar.visibility = View.VISIBLE
                XUpdate.newBuild(context)
                    .apkCacheDir(context.cacheDir.absolutePath) //设置下载缓存的根目录
                    .build()
                    .download(Constants.getAppDownloadUrl(), object : OnFileDownloadListener {
                        //设置下载的地址和下载的监听
                        override fun onStart() {

                        }

                        override fun onProgress(progress: Float, total: Long) {
                            pb_download.progress = (progress * 100).toInt()
                        }

                        override fun onCompleted(file: File): Boolean {
                            btn_download.isEnabled = true
                            btn_download.setText(R.string.install)
                            block_bottom_bar.visibility = View.VISIBLE
                            mFileUrl = file.absolutePath
                            return false
                        }

                        override fun onError(throwable: Throwable) {
                            btn_download.isEnabled = true
                            btn_download.setText(R.string.update)
                            block_bottom_bar.visibility = View.VISIBLE
                            block_progress_bar.visibility = View.GONE
                            ToastUtil.showToastInCenter(context,
                                context.getString(R.string.download_fail))
                        }
                    })
            },unGrantFun= {
                ToastUtil.showToastInCenter(context,
                    context.getString(R.string.denied_read_memory_card))
                mOnDownloadCallBack.onDownloadError()
            })
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startInstallPermissionSettingActivity() {
        val packageURI: Uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
        //注意这个是8.0新API
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)
        activity.startActivity(intent)
    }

    //安裝更新
    private fun installApk() {
        _XUpdate.startInstallApk(context, File(mFileUrl))//填写文件所在的路径
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

    override fun initHeightParams() = LayoutParams.WRAP_CONTENT

    override fun initWidthParams() = LayoutParams.MATCH_PARENT

}