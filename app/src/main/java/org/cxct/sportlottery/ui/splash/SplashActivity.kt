package org.cxct.sportlottery.ui.splash

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_splash.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.doOnResume
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.NAME_LOGIN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.system.exitProcess


/**
 * @app_destination 啟動頁
 */
class SplashActivity : BaseSocketActivity<SplashViewModel>(SplashViewModel::class) {

    private val mVersionUpdateViewModel: VersionUpdateViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).statusBarDarkFont(true).transparentStatusBar()
            .fitsSystemWindows(false).init()
        setContentView(R.layout.activity_splash)
        loading()
        setupVersion()
        //checkPermissionGranted()
        initObserve()
        //流程: 檢查/獲取 host -> 獲取 config -> 檢查維護狀態 -> 檢查版本更新 -> 跳轉畫面
        checkLocalHost()
        // 避免Not allowed to start service Intent异常
        doOnResume(once = true) { startService(Intent(this,BackService::class.java)) }
        registerBroadcast()
    }
    private fun registerBroadcast(){
        val filter= IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
//        LocalBroadcastManager.getInstance(this).registerReceiver(NetBroadcastReceiver(),filter)
    }

    private fun setupVersion() {
        val version = BuildConfig.VERSION_NAME
        tv_version_info.text = version
    }

    private fun checkLocalHost() {
        viewModel.checkLocalHost()
    }

    private fun goHomePage() {
        startActivity(Intent(this@SplashActivity, MainTabActivity::class.java))
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
        dialog.show(supportFragmentManager, null)
    }

    //檢查版本: 當前版號小於最小更新版號 => 強制更新
    private fun checkAppMinVersion() {
        mVersionUpdateViewModel.checkAppMinVersion()
    }

    private fun initObserve() {
        viewModel.errorResultIndex.observe(this) {
            Timber.d("href:Jump to internal web: =====>")
            JumpUtil.toInternalWeb(this, it, "", toolbarVisibility = false, backEvent = false)
        }

        viewModel.configResult.observe(this) {
            //判断用户是否手动设置了语言

            val languageArr = it?.configData?.supportLanguage?.split(",")
            val systemLanStr: String =
                LanguageManager.getSelectLanguage(applicationContext).key
            //1判断当前系统语言我们是否支持 如果支持使用系统语言
            if (languageArr != null && !(languageArr.contains(systemLanStr))) {
                //2如果不支持默认使用后台设置的第一种语言
                val target = LanguageManager.Language.values().find { it.key == languageArr[0] }
                if (target != null) {
                    LanguageManager.saveSelectLanguage(applicationContext, target)
                }else{
                    LanguageManager.saveSelectLanguage(applicationContext, LanguageManager.Language.EN)
                }
                viewModel.getConfig()
                return@observe
            }
            KvUtils.put(KvUtils.MARKET_SWITCH,
                isGooglePlayVersion() && BuildConfig.VERSION_NAME == it?.configData?.reviewedVersionUrl)
            when {
                it?.configData?.maintainStatus == FLAG_OPEN -> {
                    goMaintenancePage()
                }
                it?.success == true -> checkAppMinVersion()

                else -> showErrorRetryDialog(
                    getString(R.string.error_config_title),
                    getString(R.string.message_network_no_connect)
                )
            }
        }

        mVersionUpdateViewModel.appMinVersionState.observe(this) {
            if (it.isForceUpdate || it.isShowUpdateDialog)
                showAppDownloadDialog(it.isForceUpdate, it.version, it.checkAppVersionResult)
            else
                viewModel.goNextPage()
        }

        viewModel.skipHomePage.observe(this) {
            if (sConfigData?.maintainStatus == FLAG_OPEN) {
                goMaintenancePage()
                return@observe
            }
            //有banenr图片并且开关打开
            val imageUrls = sConfigData?.imageList?.filter {
                it.imageType == 9
                        && it.lang == LanguageManager.getSelectLanguage(this).key
                        && !it.imageName1.isNullOrEmpty()
                        && it.startType == (if (KvUtils.decodeBooleanTure("isFirstOpen", true)
                    && !(getMarketSwitch() && it.isHidden)
                ) 0 else 1)
            }
                ?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })
                ?.map {
                    it.imageName1!!
                }

            if (imageUrls?.isEmpty() == false && sConfigData?.androidCarouselStatus?.toIntS(0) == 1) {
                LaunchActivity.start(this, it, imageUrls = ArrayList(imageUrls))
                finish()
            } else {
                KvUtils.put("isFirstOpen", false)
                goHomePage()
            }
        }

        viewModel.isLogin.observe(this) {
            if (sConfigData?.maintainStatus == FLAG_OPEN) {
                goMaintenancePage()
            }
        }
    }

    //提示APP更新對話框
    private fun showAppDownloadDialog(
        isForceUpdate: Boolean,
        lastVersion: String,
        checkAppVersionResult: CheckAppVersionResult?,
    ) {
        AppDownloadDialog(this,
            isForceUpdate,
            lastVersion,
            checkAppVersionResult,
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

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        exitProcess(0)
    }
}
