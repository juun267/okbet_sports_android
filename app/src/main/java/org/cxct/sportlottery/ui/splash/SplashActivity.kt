package org.cxct.sportlottery.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_splash.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.home.MainActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity<SplashViewModel>(SplashViewModel::class) {

    private val mVersionUpdateViewModel: VersionUpdateViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setupVersion()
        initObserve()

        //流程: 檢查/獲取 host -> 獲取 config -> 檢查維護狀態 -> 檢查版本更新 -> 跳轉畫面
        checkLocalHost()

        //TODO simon test 之後放正式圖片就可以刪掉了
        iv_bg.scaleType = ImageView.ScaleType.FIT_CENTER
        Glide.with(this).asGif().load(R.mipmap.bg_test).into(iv_bg)
    }

    private fun setupVersion() {
        val version = BuildConfig.VERSION_CODE.toString() + "_" + BuildConfig.VERSION_NAME
        tv_version_info.text = version
    }

    private fun checkLocalHost() {
        viewModel.checkLocalHost()
    }

    private fun goHomePage() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    private fun goMaintenancePage() {
        startActivity(Intent(this@SplashActivity, MaintenanceActivity::class.java))
        finish()
    }

    //過程中任一流程請求失敗，點擊確定按鈕重試
    private fun showErrorRetryDialog(message: String) {
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(message)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.setNegativeButtonText(null)
        dialog.setPositiveButtonText(getString(R.string.btn_retry))
        dialog.setPositiveClickListener(View.OnClickListener {
            viewModel.getHost()
            dialog.dismiss()
        })
        dialog.show()
    }

    //檢查版本: 當前版號小於最小更新版號 => 強制更新
    private fun checkAppMinVersion() {
        mVersionUpdateViewModel.checkAppMinVersion()
    }

    private fun initObserve() {
        viewModel.configResult.observe(this, Observer {
            when {
                it?.configData?.maintainStatus == FLAG_OPEN -> {
                    goMaintenancePage()
                }
                it?.success == true -> checkAppMinVersion()
                else -> showErrorRetryDialog(getString(R.string.error_config))
            }
        })

        mVersionUpdateViewModel.appMinVersionState.observe(this, Observer {
            if (it.isForceUpdate || it.isShowUpdateDialog)
                showAppUpdateDialog(it.isForceUpdate, it.version)
            else
                goHomePage()
        })
    }

    //提示APP更新對話框
    private fun showAppUpdateDialog(isForceUpdate: Boolean, lastVersion: String) {
        val dialog = CustomAlertDialog(this)
        dialog.setTitle(getString(R.string.version_update))
        dialog.setMessage("【${getString(R.string.app_name)}】${getString(R.string.find_new_version)} $lastVersion")
        dialog.setCanceledOnTouchOutside(false) //設置無法點擊外部關閉
        dialog.setCancelable(false) //設置無法點擊 Back 關閉
        if (isForceUpdate) //若為強制更新隱藏取消按鈕
            dialog.setNegativeButtonText(null)
        dialog.setNegativeClickListener(View.OnClickListener {
            goHomePage()
            mVersionUpdateViewModel.lastShowUpdateTime = System.currentTimeMillis() //點擊取消 => 記錄此次提醒時間
            dialog.dismiss()
        })
        dialog.setPositiveButtonText(getString(R.string.update_new))
        dialog.setPositiveClickListener(View.OnClickListener {
            showAppDownloadDialog(isForceUpdate)
            dialog.dismiss()
        })
        dialog.show()
    }

    //APP下載對話框
    private fun showAppDownloadDialog(isForceUpdate: Boolean) {
        AppDownloadDialog(this, isForceUpdate, object : AppDownloadDialog.OnDownloadCallBack {
            override fun onDownloadError() {
                startActivity(Intent(this@SplashActivity, SplashActivity::class.java))
                finish()
            }

            override fun goHomeActivity() {
                goHomePage()
            }

        }).show()
    }
}
