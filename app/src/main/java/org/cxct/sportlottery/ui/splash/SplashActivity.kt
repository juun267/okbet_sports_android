package org.cxct.sportlottery.ui.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_splash.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.permission.GooglePermissionActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity<SplashViewModel>(SplashViewModel::class) {

    private val mVersionUpdateViewModel: VersionUpdateViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setupVersion()
        checkPermissionGranted()
        initObserve()

        //流程: 檢查/獲取 host -> 獲取 config -> 檢查維護狀態 -> 檢查版本更新 -> 跳轉畫面
        checkLocalHost()
    }

    private fun checkPermissionGranted() {
        if(getString(R.string.app_name) == "OKBET"){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this@SplashActivity, GooglePermissionActivity::class.java))
            }
        }
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

    private fun goGamePage() {
        startActivity(Intent(this@SplashActivity, GameActivity::class.java))
        finish()
    }

    private fun goMaintenancePage() {
        startActivity(Intent(this@SplashActivity, MaintenanceActivity::class.java))
        finish()
    }

    //過程中任一流程請求失敗，點擊確定按鈕重試
    private fun showErrorRetryDialog(title: String, message: String) {
        val dialog = CustomAlertDialog(this)
        dialog.setTitle(title)
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
        viewModel.configResult.observe(this) {
            when {
                it?.configData?.maintainStatus == FLAG_OPEN -> {
                    goMaintenancePage()
                }
                it?.success == true -> checkAppMinVersion()
                else -> showErrorRetryDialog(getString(R.string.error_config_title), getString(R.string.error_config))
            }
        }

        mVersionUpdateViewModel.appMinVersionState.observe(this) {
            if (it.isForceUpdate || it.isShowUpdateDialog)
                showAppDownloadDialog(it.isForceUpdate, it.version)
            else
                viewModel.goNextPage()
        }

        viewModel.skipHomePage.observe(this) {
            when (it) {
                true -> {
                    goGamePage()
                }
                false -> {
                    goHomePage()
                }
            }
        }
    }

    //提示APP更新對話框
    private fun showAppDownloadDialog(isForceUpdate: Boolean, lastVersion: String) {
        AppDownloadDialog(
            this,
            isForceUpdate,
            lastVersion,
            object : AppDownloadDialog.OnDownloadCallBack {
                override fun onDownloadError() {
                    startActivity(Intent(this@SplashActivity, SplashActivity::class.java))
                    finish()
                }

                override fun goHomeActivity() {
                    mVersionUpdateViewModel.lastShowUpdateTime =
                        System.currentTimeMillis() //點擊取消 => 記錄此次提醒時間
                    viewModel.goNextPage()
                }

            }).show()
    }
}
