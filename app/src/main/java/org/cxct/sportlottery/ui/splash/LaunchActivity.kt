package org.cxct.sportlottery.ui.splash

import android.content.Context
import android.content.Intent
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.gyf.immersionbar.ImmersionBar
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.RectangleIndicator
import com.youth.banner.listener.OnPageChangeListener
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.databinding.ActivityLaunchBinding
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.util.KvUtils


/**
 * @app_destination 啟動頁
 */
class LaunchActivity : BindingActivity<SplashViewModel,ActivityLaunchBinding>() {

    private val skipHomePage by lazy { intent.getBooleanExtra("skipHomePage", true) }
    private val imageUrls by lazy { intent.getSerializableExtra("imageUrls") as ArrayList<String> }
    private val isFirstOpen by lazy { KvUtils.decodeBooleanTure("isFirstOpen", true) }
    private val delayTime by lazy { (sConfigData?.carouselInterval?.toIntS(3) ?: 3) * 1000L }
    private var isClickSkip = false

    companion object {
        fun start(context: Context, skipHomePage: Boolean, imageUrls: ArrayList<String>) {
            context.startActivity(Intent(context, LaunchActivity::class.java).apply {
                putExtra("skipHomePage", skipHomePage)
                putExtra("imageUrls", imageUrls)
            })
        }
    }

    override fun onInitView(){
        ImmersionBar.with(this).statusBarDarkFont(true).transparentStatusBar()
            .fitsSystemWindows(false).init()

        setupBanner()
        binding.tvSkip.apply {
            isVisible = !isFirstOpen
            setOnClickListener {
                isClickSkip = true
                startNow()
            }
        }
        KvUtils.put("isFirstOpen", false)
    }

    private fun setupBanner() {
        val requestOptions =
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).dontTransform()
        (binding.banner as Banner<String, BannerImageAdapter<String>>).setAdapter(object :
            BannerImageAdapter<String>(imageUrls) {
            override fun onBindView(
                holder: BannerImageHolder,
                data: String?,
                position: Int,
                size: Int,
            ) {
                val url = sConfigData?.resServerHost + data

                Glide.with(holder.itemView).load(url).apply(requestOptions).into(holder.imageView)
            }
        }).setIndicator(RectangleIndicator(this)).addBannerLifecycleObserver(this) //添加生命周期观察者
            .addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {

                }

                override fun onPageSelected(position: Int) {
                    if (position == (imageUrls.size - 1)) {
                        //banner组件无法设置只循环一次，当滑动到最好一页的时候，手动去掉自动循环
                        binding.banner.stop()
                        binding.banner.isAutoLoop(false)
                        autoSkip()
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            }).setOnBannerListener { data, position ->
                if (position == imageUrls.size - 1) {
                    startNow()
                }
            }.setLoopTime(delayTime).isAutoLoop(true).start()
        if (imageUrls.size == 1) {
            autoSkip()
        }
    }

    private fun autoSkip() {
        lifecycleScope.launch {
            delay(delayTime)
            if (!isClickSkip && binding.banner.currentItem == imageUrls.size - 1) {
                startNow()
            }
        }
    }

    private fun goHomePage() {
        lifecycleScope.cancel()
        startActivity(Intent(this@LaunchActivity, MainTabActivity::class.java))
        finish()
    }


    private fun goMaintenancePage() {
        startActivity(Intent(this@LaunchActivity, MaintenanceActivity::class.java))
        finish()
    }


    private fun startNow() {
        if (sConfigData?.maintainStatus == FLAG_OPEN) {
            goMaintenancePage()
        } else {
            goHomePage()
        }
    }

}
