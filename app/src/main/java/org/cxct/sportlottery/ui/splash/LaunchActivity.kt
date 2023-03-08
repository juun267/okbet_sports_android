package org.cxct.sportlottery.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gyf.immersionbar.ImmersionBar
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import kotlinx.android.synthetic.main.activity_launch.*
import kotlinx.android.synthetic.main.activity_splash.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * @app_destination 啟動頁
 */
class LaunchActivity : BaseActivity<SplashViewModel>(SplashViewModel::class) {

    private val mVersionUpdateViewModel: VersionUpdateViewModel by viewModel()
    private val imageUrls = mutableListOf<String>()
    private val needAutoloop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .fitsSystemWindows(false)
            .init()
        setContentView(R.layout.activity_launch)
    }

    private fun setupBanner() {
        banner.setAdapter(object : BannerImageAdapter<String>(imageUrls) {
            override fun onBindView(
                holder: BannerImageHolder,
                data: String,
                position: Int,
                size: Int,
            ) {
                //图片加载自己实现
                Glide.with(holder.itemView)
                    .load(data)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                    .into(holder.imageView)
            }
        } as Nothing?)
            .addBannerLifecycleObserver(this) //添加生命周期观察者
            .setIndicator(CircleIndicator(this))
            .setOnBannerListener { data, position ->

            }
        if (needAutoloop) {
            banner.isAutoLoop(true)
                .setLoopTime(5000)
                .start()
        }
    }

    private fun setupVersion() {
        val version = BuildConfig.VERSION_NAME
        tv_version_info.text = version
    }

    private fun checkLocalHost() {
        viewModel.checkLocalHost()
    }

    private fun goHomePage() {
        startActivity(Intent(this@LaunchActivity, MainTabActivity::class.java))
        finish()
    }


    private fun goMaintenancePage() {
        startActivity(Intent(this@LaunchActivity, MaintenanceActivity::class.java))
        finish()
    }

    private fun goGamePublicityPage() {
//        startActivity(Intent(this@SplashActivity, GamePublicityActivity::class.java))
        startActivity(Intent(this@LaunchActivity, MainTabActivity::class.java))
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
            JumpUtil.toInternalWeb(this, it, "", toolbarVisibility = false, backEvent = false)
        }

        viewModel.configResult.observe(this) {
            when {
                it?.configData?.maintainStatus == FLAG_OPEN -> {
                    goMaintenancePage()
                }
                it?.success == true -> checkAppMinVersion()

                else -> showErrorRetryDialog(getString(R.string.error_config_title),
                    getString(R.string.message_network_no_connect))
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
            when (it) {
                true -> {
                    goGamePublicityPage()
                }
                false -> {
                    goHomePage()
                }
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
        AppDownloadDialog(
            this,
            isForceUpdate,
            lastVersion,
            checkAppVersionResult,
            object : AppDownloadDialog.OnDownloadCallBack {
                override fun onDownloadError() {
                    startActivity(Intent(this@LaunchActivity, LaunchActivity::class.java))
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
