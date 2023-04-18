package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.stx.xhb.androidx.XBanner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.ui.maintab.games.adapter.GamesTabAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.view.IndicatorWidget


class OKGamesTopView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayoutCompat(context, attrs, defStyle), XBanner.OnItemClickListener {

    private val edtSearch: EditText by lazy { findViewById(R.id.edtSearchGames) }
    private val indicatorView: IndicatorWidget by lazy { findViewById(R.id.indicatorView) }
    private val okgamesBanner: XBanner by lazy {
        findViewById<XBanner>(R.id.xbanner).apply { setOnItemClickListener(this@OKGamesTopView) }
    }

    var onSearchTextChanged: ((String) -> Unit)? = null
    var onTableClick: ((Int) -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_okgames_top, this, true)
        initView()
    }

    private fun initView() {
        initIndicator()
        setupTables()
        findViewById<View>(R.id.ivSearch).setOnClickListener{ onSearchTextChanged?.invoke(edtSearch.text.toString()) }
        findViewById<View>(R.id.searchLayout).background = DrawableCreator.Builder()
            .setSolidColor(Color.WHITE)
            .setCornersRadius(8.dp.toFloat())
            .build()

        initBanner()
        setUpBannerData()
    }

    private fun initBanner() {

    }

    private fun initIndicator() {
        val w = 12.dp.toFloat()
        val h = 4.dp.toFloat()
        val color = resources.getColor(R.color.color_7599FF)
        indicatorView.itemPadding = 1.dp
        indicatorView.defaultDrawable = createIndicatorDrawable(h, h, color, 0.5f)
        indicatorView.selectedDrawable = createIndicatorDrawable(w, h, color, 1f)
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

    private fun setupTables() {
        val rcvGamesTab = findViewById<RecyclerView>(R.id.rcvGamesTab)
        rcvGamesTab.addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_8))
        rcvGamesTab.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rcvGamesTab.adapter = GamesTabAdapter{ onTableClick?.invoke(it) }
    }

    private fun setUpBannerData() {

        var imageList = sConfigData?.imageList?.filter { it.imageType == 12 }
        val loopEnable = imageList?.size ?: 0 > 1
        indicatorView.isVisible = loopEnable

        if (imageList.isNullOrEmpty()) {
            return
        }

        okgamesBanner.setHandLoop(loopEnable)
        okgamesBanner.setAutoPlayAble(loopEnable)
        okgamesBanner.setOnItemClickListener(this)
        okgamesBanner.loadImage { _, model, view, _ ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
        }

        val host = sConfigData?.resServerHost
        val images = imageList.map{ XBannerImage(it.imageText1 + "", host + it.imgUrl, it.imageLink) }

//        val images = mutableListOf(Image("image1", "https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"),
//        Image("image2", "https://images.pexels.com/photos/709552/pexels-photo-709552.jpeg?auto=compress&cs=tinysrgb&w=1600"),
//            Image("image3", "https://images.pexels.com/photos/2378278/pexels-photo-2378278.jpeg?auto=compress&cs=tinysrgb&w=1600"),
//            Image("image4", "https://images.pexels.com/photos/268533/pexels-photo-268533.jpeg?auto=compress&cs=tinysrgb&w=1600"),
//            Image("image5", "https://images.pexels.com/photos/7459424/pexels-photo-7459424.jpeg?auto=compress&cs=tinysrgb&w=1600"),
//            Image("image6", "https://images.pexels.com/photos/267151/pexels-photo-267151.jpeg?auto=compress&cs=tinysrgb&w=1600"),
//        )

        okgamesBanner.setBannerData(images.toMutableList())
        indicatorView.setupIndicator(okgamesBanner.realCount)
        okgamesBanner.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                indicatorView.update(position % okgamesBanner.realCount)
            }
        })
    }

    override fun onItemClick(banner: XBanner, model: Any, view: View, position: Int) {
        val jumpUrl = (model as XBannerImage).jumpUrl
        if (jumpUrl.isEmptyStr()) {
            return
        }
        if (jumpUrl!!.contains("sweepstakes")) {
            JumpUtil.toLottery(context, Constants.getLotteryH5Url(context, LoginRepository.token))
        } else {
            JumpUtil.toInternalWeb(context, jumpUrl, "")
        }
    }



}