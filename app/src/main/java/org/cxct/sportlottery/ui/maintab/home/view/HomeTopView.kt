package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.stx.xhb.androidx.XBanner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.view.IndicatorWidget

class HomeTopView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle) {

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_home_top, this, true)
        initBanner()
    }

    private fun initBanner() {
        val lang = LanguageManager.getSelectLanguage(context).key
        setUpBanner(lang,2, R.id.topBanner, R.id.topBannerIndicator, resources.getColor(R.color.color_326BFF), 30.dp)
        setUpBanner(lang, 12, R.id.promotionsBanner, R.id.promotionsBannerIndicator, resources.getColor(R.color.color_7599FF), 12.dp)
    }

    private fun setUpBanner(lang: String,
                            imageType: Int,
                            bannerId: Int,
                            indicatorId: Int,
                            indicatorColor: Int,
                            indicatorSelectedWidth: Int) {


        var imageList = sConfigData?.imageList?.filter {
            it.imageType == imageType && it.lang == lang && !it.imageName1.isNullOrEmpty()
        }?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })

        val loopEnable = imageList?.size ?: 0 > 1
        if (imageList.isNullOrEmpty()) {
            return
        }

        val xbanner = findViewById<XBanner>(bannerId)
        val indicator = findViewById<IndicatorWidget>(indicatorId)

        initIndicator(indicator, indicatorColor, indicatorSelectedWidth)
        xbanner.setHandLoop(loopEnable)
        xbanner.setAutoPlayAble(loopEnable)
        xbanner.setOnItemClickListener { banner, model, view, position -> }
        xbanner.loadImage { _, model, view, _ ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
        }

        val host = sConfigData?.resServerHost
        val images = imageList.map {
            XBannerImage(it.imageText1 + "", host + it.imageName1, it.imageLink)
        }

        xbanner.setBannerData(images.toMutableList())
        indicator.setupIndicator(xbanner.realCount)
        xbanner.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                indicator.update(position % xbanner.realCount)
            }
        })
    }

    private fun initIndicator(indicator: IndicatorWidget,
                              indicatorColor: Int,
                              indicatorSelectedWidth: Int) {
        val w = indicatorSelectedWidth.toFloat()
        val h = 4.dp.toFloat()
        indicator.itemPadding = 1.5f.dp
        indicator.defaultDrawable = createIndicatorDrawable(h, h, indicatorColor, 0.5f)
        indicator.selectedDrawable = createIndicatorDrawable(w, h, indicatorColor, 1f)
    }

    private fun createIndicatorDrawable(width: Float, height: Float, color: Int, alpha: Float): Drawable {
        return DrawableCreator.Builder()
            .setSolidColor(color)
            .setShapeAlpha(alpha)
            .setCornersRadius(width)
            .setSizeHeight(height)
            .setSizeWidth(width)
            .build()
    }
}