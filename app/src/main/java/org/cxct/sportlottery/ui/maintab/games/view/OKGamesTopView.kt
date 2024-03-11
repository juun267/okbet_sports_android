package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.stx.xhb.androidx.XBanner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.LayoutOkgamesTopBinding
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
import splitties.systemservices.layoutInflater


class OKGamesTopView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(context, attrs, defStyle), XBanner.OnItemClickListener {

     val binding by lazy { LayoutOkgamesTopBinding.inflate(layoutInflater,this, true) }
    private lateinit var gameTabAdapter: GamesTabAdapter
    private var p3ogProviderFirstPosi: Int = 0
    private var p3ogProviderLastPosi: Int = 3
    private val providersAdapter by lazy { OkGameProvidersAdapter() }

    var onSearchTextChanged: ((String) -> Unit)? = null
    var onTableClick: ((OKGameTab) -> Boolean)? = null

    init {
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
        binding.edtSearchGames.onConfirm { key -> onSearchTextChanged?.invoke(key) }
        binding.ivSearch.setOnClickListener { onSearchTextChanged?.invoke(binding.edtSearchGames.text.toString()) }
        binding.searchLayout.background = DrawableCreator.Builder()
            .setSolidColor(Color.WHITE)
            .setCornersRadius(8.dp.toFloat())
            .build()
    }

    private fun initIndicator() {
        val w = 12.dp.toFloat()
        val h = 4.dp.toFloat()
        val color = resources.getColor(R.color.color_7599FF)
        binding.indicatorView.itemPadding = 1.dp
        binding.indicatorView.defaultDrawable = createIndicatorDrawable(h, h, color, 0.5f)
        binding.indicatorView.selectedDrawable = createIndicatorDrawable(w, h, color, 1f)
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

    fun setupTables()=binding.rcvGamesTab.run {
        addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_8))
        setLinearLayoutManager(RecyclerView.HORIZONTAL)
        adapter = gameTabAdapter
    }

    fun backAll() {
        gameTabAdapter.backToAll()
        binding.rcvGamesTab.smoothScrollToPosition(0)
    }

    fun changeSelectedGameTab(tab: OKGameTab) {
        val position = gameTabAdapter.changeSelectedTab(tab)
        if (position >= 0) {
            binding.rcvGamesTab.smoothScrollToPosition(position)
        }
    }

    private fun setUpBannerData(imgType: Int) {
        val lang = LanguageManager.getSelectLanguage(context).key
        var imageList = sConfigData?.imageList?.filter {
            it.imageType == imgType && it.lang == lang && !it.imageName1.isNullOrEmpty() && !(getMarketSwitch() && it.isHidden)
        }
            ?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })

        val loopEnable = imageList?.size ?: 0 > 1
        binding.indicatorView.isVisible = loopEnable

        if (imageList.isNullOrEmpty()) {
            binding.bannerCard.visibility = GONE
            return
        }
        binding.bannerCard.visibility = visibility
        binding.xbanner.apply {
            setHandLoop(loopEnable)
            setAutoPlayAble(loopEnable)
            setOnItemClickListener(this@OKGamesTopView)
            loadImage { _, model, view, _ ->
                (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
            }
        }


        val host = sConfigData?.resServerHost
        val images = imageList.map {
            XBannerImage(it.imageText1 + "", host + it.imageName1, it.appUrl)
        }
        binding.xbanner.setBannerData(images.toMutableList())
        binding.indicatorView.setupIndicator(binding.xbanner.realCount)
        binding.xbanner.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                binding.indicatorView.update(position % binding.xbanner.realCount)
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
    fun initProvider()=binding.run{
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
    fun setProviderArrowVisible(visible: Boolean)=binding.run{
        if (visible)
             setViewVisible(ivProvidersLeft, ivProvidersRight)
        else
             setViewGone(ivProvidersLeft, ivProvidersRight)
    }
    fun setProviderVisible(visible: Boolean)=binding.run{
        if (visible)
            setViewVisible(rvOkgameProviders, okgameP3LayoutProivder)
        else
            setViewGone(rvOkgameProviders, okgameP3LayoutProivder)
    }
    fun setProviderItems(firmList: MutableList<OKGamesFirm>){
        providersAdapter.setNewInstance(firmList)
    }
    fun setSeachText(key: String){
        binding.edtSearchGames.setText(key)
    }

}