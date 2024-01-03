package org.cxct.sportlottery.ui.splash

import android.content.Intent
import android.view.View
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.postDelayed
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivitySplashBinding
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.common.WebActivity
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
class SplashActivity : BindingActivity<SplashViewModel,ActivitySplashBinding>() {

    private val mVersionUpdateViewModel: VersionUpdateViewModel by viewModel()
    private var enterTime=0L

    override fun onInitView() {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            .fitsSystemWindows(false)
            .init()
        //加载缓存的启动图
        loadSplash()
        loading()
        setupVersion()

        initObserve()
        //流程: 檢查/獲取 host -> 獲取 config -> 檢查維護狀態 -> 檢查版本更新 -> 跳轉畫面
        checkLocalHost()
    }

    private fun setupVersion() {
        binding.tvVersionInfo.text = BuildConfig.VERSION_NAME
    }

    private fun checkLocalHost() {
        enterTime=System.currentTimeMillis()
        viewModel.checkLocalHost()
    }




    private fun goHomePage() {
        startActivity(Intent(this@SplashActivity, MainTabActivity::class.java))
        finish()
    }


    private fun goMaintenancePage() {
        postDelayed(getSplashTime()){
            startActivity(Intent(this@SplashActivity, MaintenanceActivity::class.java))
            finish()
        }
    }

    private  fun sendToLaunch(flag:Boolean,imageUrls:ArrayList<String>){
        postDelayed(getSplashTime()){
            LaunchActivity.start(this, flag, imageUrls =imageUrls)
            finish()
        }
    }

    private fun getSplashTime():Long{
        val nowTime=System.currentTimeMillis()
        var countTime =2500-(nowTime-enterTime)
        if(countTime<0){
            countTime=0
        }
        return countTime
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
            JumpUtil.toInternalWeb(this, it, "", toolbarVisibility = false, backEvent = false,tag = WebActivity.TAG_403)
        }

        viewModel.configResult.observe(this) { configResult ->
            // 进入维护页面不处理
            if (configResult==null&&!viewModel.errorResultIndex.value.isNullOrEmpty()){
                return@observe
            }
            //判断用户是否手动设置了语言

            val languageArr = configResult?.configData?.supportLanguage?.split(",")
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



            //启动图
            val splashImage=sConfigData?.imageList?.filter {it.imageType==21&& it.lang == systemLanStr}?.sortedByDescending { it.imageSort }
            if(!splashImage.isNullOrEmpty()){
                //加载启动图
                loadSplash("${sConfigData?.resServerHost}${splashImage[0].imageName1}")
            }

            if(isGooglePlayVersion()){
                KvUtils.put(KvUtils.MARKET_SWITCH,(sConfigData?.reviewedVersionUrl?.contains(BuildConfig.VERSION_NAME)==true))
            }else{
                KvUtils.put(KvUtils.MARKET_SWITCH,false)
            }
            sendToMain(configResult)
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
                sendToLaunch(it,ArrayList(imageUrls))
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

    override fun onResume() {
        super.onResume()
        isStoped = false
    }

    private var isStoped = false
    override fun onStop() {
        super.onStop()
        if (isSkiped) {
            window.setBackgroundDrawable(null)
        }
    }

    private var isSkiped = false
    override fun startActivity(intent: Intent) {
        if (!isSkiped && intent.component?.packageName == packageName) {
            isSkiped = true
        }
        if(!isStoped) {
            super.startActivity(intent)
        }
    }

    private fun sendToMain(config: ConfigResult?){
        when {
            config?.configData?.maintainStatus == FLAG_OPEN -> {
                goMaintenancePage()
            }
            config?.success == true -> checkAppMinVersion()
            else -> showErrorRetryDialog(
                getString(R.string.error_config_title),
                getString(R.string.message_network_no_connect)
            )
        }
    }

    //加载启动图
    private val splashKeyStr="splashAd"
    private fun loadSplash(url:String="")=binding.ivSplash.run{
        val localUrl = KvUtils.decodeString(splashKeyStr)
        if(url.isEmpty()){
                if (localUrl.isNotEmpty()){
                    visible()
                    load( localUrl)
                }
            }else{
                if (localUrl==url){
                    return
                }
                visible()
                load(url)
                KvUtils.put(splashKeyStr,url)
            }
    }

}
