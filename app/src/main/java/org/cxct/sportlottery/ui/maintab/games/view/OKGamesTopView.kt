package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.stx.xhb.androidx.XBanner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.ui.maintab.games.OkGameProvidersAdapter
import org.cxct.sportlottery.ui.maintab.games.adapter.GamesTabAdapter
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.util.getMarketSwitch
import org.cxct.sportlottery.view.IndicatorWidget


class OKGamesTopView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(context, attrs, defStyle), XBanner.OnItemClickListener {

    val edtSearch: EditText by lazy { findViewById(R.id.edtSearchGames) }
    private val indicatorView: IndicatorWidget by lazy { findViewById(R.id.indicatorView) }
    private lateinit var gameTabAdapter: GamesTabAdapter
    private val rcvGamesTab by lazy { findViewById<RecyclerView>(R.id.rcvGamesTab) }
    private val bannerCard by lazy { findViewById<View>(R.id.bannerCard) }
    private val rvOkgameProviders by lazy { findViewById<RecyclerView>(R.id.rv_okgame_providers) }
    private val okgameP3LayoutProivder by lazy { findViewById<LinearLayout>(R.id.okgame_p3_layout_proivder) }
    private val ivProvidersLeft by lazy { findViewById<ImageView>(R.id.iv_providers_left) }
    private val ivProvidersRight by lazy { findViewById<ImageView>(R.id.iv_providers_right) }
    private var p3ogProviderFirstPosi: Int = 0
    private var p3ogProviderLastPosi: Int = 3
    private val providersAdapter by lazy { OkGameProvidersAdapter() }

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
        initProvider()
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
            it.imageType == imgType && it.lang == lang && !it.imageName1.isNullOrEmpty() && !(getMarketSwitch() && it.isHidden)
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
    fun initProvider(){
        ivProvidersLeft.alpha = 0.5F

        var okGameProLLM = rvOkgameProviders.setLinearLayoutManager(LinearLayoutManager.HORIZONTAL)
        rvOkgameProviders.adapter = providersAdapter
        rvOkgameProviders.layoutManager = okGameProLLM
        if (rvOkgameProviders.itemDecorationCount==0) {
            rvOkgameProviders.addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_8))
        }
        rvOkgameProviders.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rvView: RecyclerView, newState: Int) {
                // 获取当前滚动到的条目位置
                p3ogProviderFirstPosi = okGameProLLM.findFirstVisibleItemPosition()
                p3ogProviderLastPosi = okGameProLLM.findLastVisibleItemPosition()
                ivProvidersLeft.isClickable = p3ogProviderFirstPosi > 0

                if (p3ogProviderFirstPosi > 0) {
                    ivProvidersLeft.alpha = 1F
                } else {
                    ivProvidersLeft.alpha = 0.5F
                }
                if (p3ogProviderLastPosi == providersAdapter.data.size - 1) {
                    ivProvidersRight.alpha = 0.5F
                } else {
                    ivProvidersRight.alpha = 1F
                }

                ivProvidersRight.isClickable = p3ogProviderLastPosi != providersAdapter.data.size - 1
            }
        })
        //供应商左滑按钮
        ivProvidersLeft.setOnClickListener {
            if (p3ogProviderFirstPosi >= 3) {
                rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    rvOkgameProviders,
                    RecyclerView.State(),
                    p3ogProviderFirstPosi - 2
                )
            } else {
                rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    rvOkgameProviders, RecyclerView.State(), 0
                )
            }
        }
        //供应商右滑按钮
        ivProvidersRight.setOnClickListener {
            if (p3ogProviderLastPosi < providersAdapter.data.size - 4) {
                rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    rvOkgameProviders,
                    RecyclerView.State(),
                    p3ogProviderLastPosi + 2
                )
            } else {
                rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    rvOkgameProviders,
                    RecyclerView.State(),
                    providersAdapter.data.size - 1
                )
            }
        }
    }
    fun setProviderSelect(onProviderSelect: (OKGamesFirm)->Unit){
        providersAdapter.setOnItemClickListener { _, _, position ->
            onProviderSelect.invoke(providersAdapter.getItem(position))
        }
    }
    fun setProviderArrowVisible(visible: Boolean){
        if (visible)
             setViewVisible(ivProvidersLeft, ivProvidersRight)
        else
             setViewGone(ivProvidersLeft, ivProvidersRight)
    }
    fun setProviderVisible(visible: Boolean){
        if (visible)
            setViewVisible(rvOkgameProviders, okgameP3LayoutProivder)
        else
            setViewGone(rvOkgameProviders, okgameP3LayoutProivder)
    }
    fun setProviderItems(firmList: MutableList<OKGamesFirm>){
        providersAdapter.setNewInstance(firmList)
    }

}