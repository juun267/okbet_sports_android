package org.cxct.sportlottery.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.dialog_app_download.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.util.AppUpdateManager
import org.cxct.sportlottery.util.ToastUtil

class AppDownloadDialog(
    val activity: FragmentActivity,
    private val mIsForce: Boolean,
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
            if (btn_download.text == context.getString(R.string.btn_download))
                doInternalDownload()
            else
                doUpdate()
        }
    }

    //內部下載
    @SuppressLint("CheckResult")
    private fun doInternalDownload() {
        //先請求存取權限，再行下載
        mRxPermissions
            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { aBoolean ->
                if (aBoolean) {
                    btn_download.isEnabled = false
                    pb_update.visibility = View.VISIBLE
                    AppUpdateManager.downloadApk(context, Constants.getAppDownloadUrl(), object : AppUpdateManager.OnDownloadListener {
                        override fun onProgress(downloadBytes: Int, totalBytes: Int) {
                            val downloadStr = if (downloadBytes >= 0) downloadBytes.toString() else "-"
                            val totalStr = if (totalBytes >= 0) totalBytes.toString() else "-"
                            tv_download_byte_and_total.text = String.format("%s bytes / %s bytes", downloadStr, totalStr)
                            pb_update.progress = (downloadBytes * 1.0f / totalBytes * 100).toInt()
                        }

                        override fun onFinish(fileUrl: String) {
                            btn_download.isEnabled = true
                            btn_download.setText(R.string.update_new)
                            mFileUrl = fileUrl
                        }

                        override fun onError() {
                            btn_download.isEnabled = true
                            pb_update.visibility = View.GONE
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
    private fun doUpdate() {
        try {
            AppUpdateManager.install(context, mFileUrl)
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