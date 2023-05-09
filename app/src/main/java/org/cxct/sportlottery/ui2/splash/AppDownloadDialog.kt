package org.cxct.sportlottery.ui2.splash

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.dialog_app_download.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.util.AppUpdateManager
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setTitleLetterSpacing

class AppDownloadDialog(
    val activity: FragmentActivity,
    private val mIsForce: Boolean,
    private val mLastVersion: String,
    private val checkAppVersionResult: CheckAppVersionResult?,
    private val mOnDownloadCallBack: OnDownloadCallBack,
) : AlertDialog(activity) {

    private val mRxPermissions = RxPermissions(activity)
    private var mFileUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_app_download)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false) //設置無法點擊外部關閉
        setCancelable(false) //設置無法點擊 Back 關閉
        initView()
    }

    override fun dismiss() {
        super.dismiss()
        AppUpdateManager.cancel()
    }

    private fun initView() {
        btn_cancel.visibility = if (mIsForce) View.GONE else View.VISIBLE
        btn_cancel.setOnClickListener {
            mOnDownloadCallBack.goHomeActivity()
            dismiss()
        }

        btn_download.setOnClickListener {
            if (BuildConfig.FLAVOR != "google") {
                if (btn_download.text == context.getString(R.string.update))
                    doInternalDownload()
                else
                    doUpdate(mFileUrl)
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
        tv_new_version.text = "v$mLastVersion"

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
                            ToastUtil.showToastInCenter(context, context.getString(R.string.download_fail))
                        }
                    })
                } else {
                    ToastUtil.showToastInCenter(context, context.getString(R.string.denied_read_memory_card))
                    mOnDownloadCallBack.onDownloadError()
                }
            }
    }

    //安裝更新
    private fun doUpdate(fileUrl: String?) {
        try {
            AppUpdateManager.install(context, fileUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToastInCenter(context, context.getString(R.string.error_file))
            mOnDownloadCallBack.onDownloadError()
        }
    }

    interface OnDownloadCallBack {
        fun onDownloadError()
        fun goHomeActivity()
    }
}