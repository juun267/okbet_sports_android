package org.cxct.sportlottery.ui.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.gyf.immersionbar.ImmersionBar
import com.tencent.mmkv.MMKV
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.RectangleIndicator
import com.youth.banner.listener.OnPageChangeListener
import kotlinx.android.synthetic.main.activity_launch.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity


/**
 * @app_destination 啟動頁
 */
class LaunchActivity : BaseActivity<SplashViewModel>(SplashViewModel::class) {

    private val skipHomePage by lazy { intent.getBooleanExtra("skipHomePage", true) }
    private val imageUrls by lazy { intent.getSerializableExtra("imageUrls") as ArrayList<String> }
    private val isFirstOpen by lazy { MMKV.defaultMMKV().getBoolean("isFirstOpen", true) }

    companion object {
        fun start(context: Context, skipHomePage: Boolean, imageUrls: ArrayList<String>) {
            context.startActivity(Intent(context, LaunchActivity::class.java)
                .apply {
                    putExtra("skipHomePage", skipHomePage)
                    putExtra("imageUrls", imageUrls)
                })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .fitsSystemWindows(false)
            .init()
        setContentView(R.layout.activity_launch)

        tv_skip.isVisible = !isFirstOpen
        setupBanner()
        tv_skip.setOnClickListener {
            startNow()
        }
        MMKV.defaultMMKV().putBoolean("isFirstOpen", false)
    }

    private fun setupBanner() {
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontTransform()
        (banner as Banner<String, BannerImageAdapter<String>>)
            .setAdapter(object : BannerImageAdapter<String>(imageUrls) {
                override fun onBindView(
                    holder: BannerImageHolder,
                    data: String?,
                    position: Int,
                    size: Int,
                ) {
                    val url = sConfigData?.resServerHost + data

                    Glide.with(holder.itemView)
                        .load(url)
                        .apply(requestOptions)
                        .into(holder.imageView)
                }
            })
            .setIndicator(RectangleIndicator(this))
            .addBannerLifecycleObserver(this) //添加生命周期观察者
            .addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                    if (position == imageUrls.size - 1) {
                        autoSkip()
                    }
                }

                override fun onPageSelected(position: Int) {
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
            .setOnBannerListener { data, position ->
                if (position == imageUrls.size - 1) {
                    startNow()
                }
            }
            .setLoopTime((sConfigData?.carouselInterval?.toIntS(3) ?: 3) * 1000L)
            .isAutoLoop(false)
            .start()
    }

    private fun autoSkip() {
        GlobalScope.launch {
            delay((sConfigData?.carouselInterval?.toIntS(3) ?: 3) * 1000L)
            startNow()
        }
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
        startActivity(Intent(this@LaunchActivity, MainTabActivity::class.java))
        finish()
    }

    private fun startNow() {
        if (sConfigData?.maintainStatus == FLAG_OPEN) {
            goMaintenancePage()
        } else {
            when (skipHomePage) {
                true -> {
                    goGamePublicityPage()
                }
                false -> {
                    goHomePage()
                }
            }
        }
    }

}
