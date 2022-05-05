package org.cxct.sportlottery.ui.profileCenter.versionUpdate

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_version_update.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.view.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.AppUpdateManager
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.ToastUtil


class VersionUpdateActivity : BaseActivity<VersionUpdateViewModel>(VersionUpdateViewModel::class) {

    private val mRxPermissions = RxPermissions(this)
    private var mFileUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_version_update)

        initView()
        initButton()
        getCheckAppVersionUpdateData()
        initObserve()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppUpdateManager.cancel()
    }

    override fun loading() {
        layout_loading.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        layout_loading.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        toolBar.tv_toolbar_title.typeface = Typeface.DEFAULT
        tv_version.text = "v${BuildConfig.VERSION_CODE}_${BuildConfig.VERSION_NAME}"
        btn_internal_download.visibility = View.GONE
        btn_external_download.visibility = View.GONE
        pb_update.visibility = View.GONE
    }

    private fun initButton() {
        toolBar.setOnBackPressListener {
            finish()
        }

        //下載/更新 .apk
        btn_internal_download.setOnClickListener {
            if (btn_internal_download.text == getString(R.string.internal_download))
                doInternalDownload()
            else
                doUpdate()
        }

        //外部下載
        btn_external_download.setOnClickListener {
            JumpUtil.toExternalWeb(this, sConfigData?.mobileAppDownUrl)
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
                    btn_internal_download.isEnabled = false
                    pb_update.visibility = View.VISIBLE
                    AppUpdateManager.downloadApk(this, Constants.getAppDownloadUrl(), object : AppUpdateManager.OnDownloadListener {
                        override fun onProgress(downloadBytes: Int, totalBytes: Int) {
                            pb_update.progress = (downloadBytes * 1.0f / totalBytes * 100).toInt()
                        }

                        override fun onFinish(fileUrl: String) {
                            btn_internal_download.isEnabled = true
                            btn_internal_download.setText(R.string.update_now)
                            mFileUrl = fileUrl
                        }

                        override fun onError() {
                            btn_internal_download.isEnabled = true
                            pb_update.visibility = View.GONE
                            ToastUtil.showToastInCenter(this@VersionUpdateActivity, getString(R.string.download_fail))
                        }
                    })
                } else {
                    ToastUtil.showToastInCenter(this, getString(R.string.denied_read_memory_card))
                }
            }
    }

    //安裝更新
    private fun doUpdate() {
        try {
            AppUpdateManager.install(this, mFileUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToastInCenter(this, getString(R.string.error_file))
        }
    }

    //檢查版本
    private fun getCheckAppVersionUpdateData() {
        loading()
        viewModel.checkAppVersion()
    }

    @SuppressLint("SetTextI18n")
    private fun initObserve() {
        viewModel.appVersionState.observe(this, Observer {
            hideLoading()
            if (it.isNewVersion) {
                tv_version_content.text = "${getString(R.string.new_version)}(v${it.lastVersionCode}_${it.lastVersionName})${getString(R.string.updatable)}"

                btn_external_download.visibility = View.VISIBLE
                btn_internal_download.visibility = View.VISIBLE
            } else {
                tv_version_content.text = getString(R.string.current_versionIs_the_latest_version)

                btn_external_download.visibility = View.GONE
                btn_internal_download.visibility = View.GONE
            }
        })
    }
}