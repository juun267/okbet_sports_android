package org.cxct.sportlottery.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.FrameLayout.LayoutParams
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.dialog_app_download.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogAppDownloadBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.ui.base.BaseBindingDialog
import org.cxct.sportlottery.util.*


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
        AppUpdateManager.cancel()
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
                    checkInstall()
            } else {
                checkAppVersionResult?.let {
                    try {
                        AppUpdateManager.jumpMarketApp(context, it.storeURL ?: "")
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
            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { aBoolean ->
                if (aBoolean) {
                    block_bottom_bar.visibility = View.GONE
                    block_progress_bar.visibility = View.VISIBLE
                    AppUpdateManager.downloadApk(block_progress_bar.context, Constants.getAppDownloadUrl(), object : AppUpdateManager.OnDownloadListener {
                        override fun onProgress(downloadBytes: Int, totalBytes: Int) {
                            pb_download.progress = (downloadBytes * 1.0f / totalBytes * 100).toInt()
                        }

                        override fun onFinish(fileUrl: String) {
                            btn_download.isEnabled = true
                            btn_download.setText(R.string.install)
                            block_bottom_bar.visibility = View.VISIBLE
                            mFileUrl = fileUrl
                        }

                        override fun onError() {
                            btn_download.isEnabled = true
                            btn_download.setText(R.string.update)
                            block_bottom_bar.visibility = View.VISIBLE
                            block_progress_bar.visibility = View.GONE
                            ToastUtil.showToastInCenter(context,
                                context.getString(R.string.download_fail))
                        }
                    })
                } else {
                    ToastUtil.showToastInCenter(context,
                        context.getString(R.string.denied_read_memory_card))
                    mOnDownloadCallBack.onDownloadError()
                }
            }
    }

    private fun checkInstall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hasInstallPermission: Boolean =
                activity.packageManager.canRequestPackageInstalls()
            if (!hasInstallPermission) {
                startInstallPermissionSettingActivity()
            } else {
                //再次执行安装流程，包含权限判等
                if (mFileUrl?.isNotEmpty() == true) {
                    //再次启动安装流程
                    installApk()
                }
            }
        } else {
            installApk()
        }
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
        try {
            AppUpdateManager.install(context, mFileUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToastInCenter(context, context.getString(R.string.error_file))
            mOnDownloadCallBack?.onDownloadError()
        }
    }
    interface OnDownloadCallBack {
        fun onDownloadError()
        fun goHomeActivity()
    }

    override fun initHeightParams() = LayoutParams.WRAP_CONTENT

    override fun initWidthParams() = LayoutParams.MATCH_PARENT

}