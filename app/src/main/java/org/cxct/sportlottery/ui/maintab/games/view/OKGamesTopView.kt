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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.stx.xhb.androidx.XBanner
import com.stx.xhb.androidx.entity.BaseBannerInfo
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.maintab.games.adapter.GamesTabAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp
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
        setUpBannerData(mutableListOf())
    }

    private fun initBanner() {
        okgamesBanner.setOnItemClickListener(this)
        okgamesBanner.loadImage { _, model, view, _->
            (view as ImageView).load((model as Image).url, )
        }

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
        rcvGamesTab.adapter = GamesTabAdapter(::onTableClick)
    }

    fun setUpBannerData(list: List<String>) {

        var imageList = sConfigData?.imageList?.filter { it.imageType == 2 }

        if (imageList.isNullOrEmpty()) {
            return
        }

        val host = sConfigData?.resServerHost
//        val images = imageList.map{ Image(it.imageText1 + "", host + it.imgUrl) }
        val images = mutableListOf(Image("image1", "https://images.pexels.com/photos/417074/pexels-photo-417074.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"),
        Image("image2", "https://images.pexels.com/photos/709552/pexels-photo-709552.jpeg?auto=compress&cs=tinysrgb&w=1600"),
            Image("image3", "https://images.pexels.com/photos/2378278/pexels-photo-2378278.jpeg?auto=compress&cs=tinysrgb&w=1600"),
            Image("image4", "https://images.pexels.com/photos/268533/pexels-photo-268533.jpeg?auto=compress&cs=tinysrgb&w=1600"),
            Image("image5", "https://images.pexels.com/photos/7459424/pexels-photo-7459424.jpeg?auto=compress&cs=tinysrgb&w=1600"),
            Image("image6", "https://images.pexels.com/photos/267151/pexels-photo-267151.jpeg?auto=compress&cs=tinysrgb&w=1600"),
        )

        okgamesBanner.setBannerData(images.toMutableList())
        indicatorView.setupIndicator(okgamesBanner.realCount)
        okgamesBanner.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                indicatorView.update(position % okgamesBanner.realCount)
            }
        })
    }

    override fun onItemClick(banner: XBanner, model: Any, view: View, position: Int) {

    }

    fun onTableClick(position: Int) {

    }

    data class Image(val name: String, val url: String): BaseBannerInfo {
        override fun getXBannerUrl() = url

        override fun getXBannerTitle() = name

    }

}