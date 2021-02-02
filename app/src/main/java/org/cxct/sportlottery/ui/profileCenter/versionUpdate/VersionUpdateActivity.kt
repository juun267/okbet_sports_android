package org.cxct.sportlottery.ui.profileCenter.versionUpdate

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_version_update.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.JumpUtil

class VersionUpdateActivity : BaseActivity<VersionUpdateViewModel>(VersionUpdateViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_version_update)

        initButton()
        getCheckAppVersionUpdateData()
    }

    override fun loading() {
        layout_loading.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        layout_loading.visibility = View.GONE
    }

    private fun initButton() {
        btn_back.setOnClickListener {
            finish()
        }

        btn_internal_download.visibility = View.GONE
        btn_external_download.visibility = View.GONE
        pb_update.visibility = View.GONE

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
    private fun doInternalDownload() {
//        //先請求存取權限，再行下載
//        val permissions = RxPermissions(this)
//            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            .subscribe { aBoolean ->
//                if (aBoolean) {
//                    btn_internal_download.isEnabled = false
//                    pb_update.visibility = View.VISIBLE
//                    AppUpdateManager.downloadApk(this, object: AppUpdateManager.OnDownloadListener {
//                        override fun onProgress(downloadBytes: Int, totalBytes: Int) {
//                            pb_update.progress = (downloadBytes * 1.0f / totalBytes * 100).toInt()
//                        }
//
//                        override fun onFinish(fileUrl: String) {
//                            btn_internal_download.isEnabled = true
//                            btn_internal_download.setText(R.string.update_new)
//                            mFileUrl = fileUrl
//                        }
//
//                        override fun onError() {
//                            btn_internal_download.isEnabled = true
//                            pb_update.visibility = View.GONE
//                            ToastUtil.showToastInCenter(this@AppVersionUpdateActivity, getString(R.string.download_fail))
//                        }
//                    })
//                } else {
////                        ToastUtil.showToastInCenter(this, getString(R.string.picture_jurisdiction))
//                    ToastUtil.showToastInCenter(this, getString(R.string.denied_read_memory_card))
//                }
//            }
    }

    //安裝更新
    private fun doUpdate() {
//        try {
//            AppUpdateManager.install(this@AppVersionUpdateActivity, mFileUrl)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            ToastUtil.showToastInCenter(this@AppVersionUpdateActivity, getString(R.string.error_file))
//        }
    }

    /**
     * 檢查版本
     */
    private fun getCheckAppVersionUpdateData() {
//        loading()
//        mIndexControllerApi.checkAppUpdate(object : BaseWebApi.ResultListener {
//            @SuppressLint("SetTextI18n")
//            override fun onResult(response: String) {
//                Log.i(TAG, "檢查 更新版本 成功!!!")
//                try {
//                    hideLoading()
//                    val checkAppUpdateForAndroidOnlyOutput = getCheckAppUpdateForAndroidOnlyOutput(response)
//                    checkAppUpdateForAndroidOnlyOutput.let {
//                        var isNewVersionCode = false
//                        val versionList: ArrayList<Int> = arrayListOf()
//                        val androidVersionCode = checkAppUpdateForAndroidOnlyOutput.version?.split("_")?.get(0)
//                            ?: ""
//                        val androidVersion = checkAppUpdateForAndroidOnlyOutput.version?.split("_")?.get(1)
//                            ?: ""
//                        versionList.add(if (androidVersionCode.isBlank()) 0 else androidVersionCode.toInt())
//                        androidVersion.split(".").forEach {
//                            versionList.add(if (it.isBlank()) 0 else it.toInt())
//                        }
//
//                        val localVersionList: ArrayList<Int> = arrayListOf()
//                        localVersionList.add(BuildConfig.VERSION_CODE)
//                        BuildConfig.VERSION_NAME.split(".").forEach {
//                            localVersionList.add(if (it.isBlank()) 0 else it.toInt())
//                        }
//
//                        if (versionList.size == localVersionList.size) {
//                            for (i in versionList.indices) {
//                                if (versionList[i] > localVersionList[i]) {
//                                    isNewVersionCode = true
//                                    break
//                                }
//                                else if (versionList[i] < localVersionList[i]) {
//                                    break
//                                }
//                            }
//                        }
//
//                        tv_version.text = "v${BuildConfig.VERSION_NAME}"
//
//                        if (isNewVersionCode) {
//                            tv_version_content.text = "${getString(R.string.new_version)}(v${androidVersion})${getString(R.string.updatable)}"
//
//                            btn_external_download.visibility = View.VISIBLE
//                            btn_internal_download.visibility = View.VISIBLE
//                        } else {
//                            tv_version_content.text = getString(R.string.current_versionIs_the_latest_version)
//
//                            btn_external_download.visibility = View.GONE
//                            btn_internal_download.visibility = View.GONE
//                        }
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    onError(ErrorOutput(e.message))
//                }
//            }
//
//            override fun onError(error: ErrorOutput) {
//                Log.e(TAG, "檢查 更新版本 失敗!!!")
//                hideLoading()
//                tv_version.text = "v${BuildConfig.VERSION_CODE}_${BuildConfig.VERSION_NAME}"
//                tv_version_content.text = getString(R.string.current_versionIs_the_latest_version)
//
//                btn_external_download.visibility = View.GONE
//                btn_internal_download.visibility = View.GONE
//            }
//        })
    }
}