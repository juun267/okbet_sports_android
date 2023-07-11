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
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.stx.xhb.androidx.XBanner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.onConfirm
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.ui.maintab.games.adapter.GamesTabAdapter
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.view.IndicatorWidget


class OKGamesTopView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(context, attrs, defStyle), XBanner.OnItemClickListener {

    private val edtSearch: EditText by lazy { findViewById(R.id.edtSearchGames) }
    private val indicatorView: IndicatorWidget by lazy { findViewById(R.id.indicatorView) }
    private lateinit var gameTabAdapter: GamesTabAdapter
    private val rcvGamesTab by lazy { findViewById<RecyclerView>(R.id.rcvGamesTab) }
    private val bannerCard by lazy { findViewById<View>(R.id.bannerCard) }
    private val okgamesBanner: XBanner by lazy {
        findViewById<XBanner>(R.id.xbanner).apply { setOnItemClickListener(this@OKGamesTopView) }
    }

    var onSearchTextChanged: ((String) -> Unit)? = null
    var onTableClick: ((OKGameTab) -> Boolean)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_okgames_top, this, true)
        initView()
    }

    fun setup(lifecycleOwner: LifecycleOwner, imgType: Int, gameType: String = "okgame") {
        gameTabAdapter = GamesTabAdapter(gameType) { onTableClick?.invoke(it) ?: false }
        setupTables()
        ConfigRepository.onNewConfig(lifecycleOwner) {
            it?.let { setUpBannerData(imgType) }
        }
    }

    private fun initView() {
        initIndicator()
        initSearch()
    }

    private fun initSearch() {
        edtSearch.onConfirm { key -> onSearchTextChanged?.invoke(key) }
        findViewById<View>(R.id.ivSearch).setOnClickListener { onSearchTextChanged?.invoke(edtSearch.text.toString()) }
        findViewById<View>(R.id.searchLayout).background = DrawableCreator.Builder()
            .setSolidColor(Color.WHITE)
            .setCornersRadius(8.dp.toFloat())
            .build()
    }

    private fun initIndicator() {
        val w = 12.dp.toFloat()
        val h = 4.dp.toFloat()
        val color = resources.getColor(R.color.color_7599FF)
        indicatorView.itemPadding = 1.dp
        indicatorView.defaultDrawable = createIndicatorDrawable(h, h, color, 0.5f)
        indicatorView.selectedDrawable = createIndicatorDrawable(w, h, color, 1f)
    }

    private fun createIndicatorDrawable(
        width: Float,
        height: Float,
        color: Int,
        alpha: Float
    ): Drawable {
        return DrawableCreator.Builder()
            .setSolidColor(color)
            .setShapeAlpha(alpha)
            .setCornersRadius(width)
            .setSizeHeight(height)
            .setSizeWidth(width)
            .build()
    }

    fun setupTables() {
        rcvGamesTab.addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_8))
        rcvGamesTab.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        rcvGamesTab.adapter = gameTabAdapter
    }

    fun backAll() {
        gameTabAdapter.backToAll()
        rcvGamesTab.smoothScrollToPosition(0)
    }

    fun changeSelectedGameTab(tab: OKGameTab) {
        val position = gameTabAdapter.changeSelectedTab(tab)
        if (position >= 0) {
            rcvGamesTab.smoothScrollToPosition(position)
        }
    }

    private fun setUpBannerData(imgType: Int) {
        val lang = LanguageManager.getSelectLanguage(context).key
        var imageList = sConfigData?.imageList?.filter {
            it.imageType == imgType && it.lang == lang && !it.imageName1.isNullOrEmpty() && !(isGooglePlayVersion() && it.isHidden)
        }
            ?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })

        val loopEnable = imageList?.size ?: 0 > 1
        indicatorView.isVisible = loopEnable

        if (imageList.isNullOrEmpty()) {
            bannerCard.visibility = GONE
            return
        }
        bannerCard.visibility = visibility
        okgamesBanner.setHandLoop(loopEnable)
        okgamesBanner.setAutoPlayAble(loopEnable)
        okgamesBanner.setOnItemClickListener(this)
        okgamesBanner.loadImage { _, model, view, _ ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
        }

        val host = sConfigData?.resServerHost
        val images = imageList.map {
            XBannerImage(it.imageText1 + "", host + it.imageName1, it.appUrl)
        }

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


    fun setTabsData(tabs: MutableList<OKGameTab>?) {
        if (tabs.isNullOrEmpty()) {
            return
        }
        gameTabAdapter.addData(tabs)
    }


}