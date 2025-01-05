package org.cxct.sportlottery.ui.profileCenter.pointshop


import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.drake.spannable.setSpan
import com.google.android.material.appbar.AppBarLayout
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.UpdatePointEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityPointShopBinding
import org.cxct.sportlottery.databinding.ItemMarqueeBinding
import org.cxct.sportlottery.databinding.ViewNoProductBinding
import org.cxct.sportlottery.net.point.PointRepository
import org.cxct.sportlottery.net.point.data.Image
import org.cxct.sportlottery.net.point.data.Product
import org.cxct.sportlottery.network.quest.info.setupTaskCountDownTimer
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.publicity.MarqueeAdapter
import org.cxct.sportlottery.ui.profileCenter.pointshop.announcement.AnnouncementItem
import org.cxct.sportlottery.ui.profileCenter.pointshop.main.ShopMainViewEvent
import org.cxct.sportlottery.ui.profileCenter.pointshop.main.ShopSortedNormalTab
import org.cxct.sportlottery.ui.profileCenter.pointshop.main.ShopSortedPricesTab
import org.cxct.sportlottery.ui.profileCenter.pointshop.main.ShopTypeFilterTabImpl
import org.cxct.sportlottery.ui.profileCenter.pointshop.main.ShopTypeTabAdapter
import org.cxct.sportlottery.ui.profileCenter.pointshop.order.ExchangeGoodsDialog
import org.cxct.sportlottery.ui.profileCenter.pointshop.order.ExchangeRewardDialog
import org.cxct.sportlottery.ui.profileCenter.pointshop.record.PointExchangeActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.record.PointHistoryActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.setColors
import org.cxct.sportlottery.view.verticalMarquee.VerticalMarqueeAdapter
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

class PointShopActivity : BaseSocketActivity<PointShopViewModel, ActivityPointShopBinding>(PointShopViewModel::class) {

    private val rvShopTypeTabAdapter = ShopTypeTabAdapter()
    private val rvProductsAdapter = ShopProductsAdapter()

    override fun pageName() = "积分商城"

    private val statusBarHeight by lazy {
        ImmersionBar.getStatusBarHeight(this)
    }

    override fun onInitView() {
        setStatusbar(R.color.color_F6F7F8, true)
        binding.toolBar.setToolbarBackgroundColor(R.color.transparent)
        binding.toolBar.binding.tvToolbarTitle.setColors(R.color.color_0D2245)
        EventBusUtil.targetLifecycle(this)
        initView()
        initObservable()
        binding.marqueeView.bindLifecycler(this)
    }

    override fun onInitData() {
        super.onInitData()
        viewModel.getProductIndex()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.getProductIndex()
    }

    private fun initView() {
        binding.toolBar.setOnBackPressListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolBar.binding.ivToolbarEnd.setOnClickListener {
            startActivity(Intent(this, PointRulesActivity::class.java))
        }

        binding.clUserBlock.apply {
            setMargins(
                left = marginStart,
                top = -(statusBarHeight + 54.dp),
                right = marginEnd,
                bottom = marginBottom
            )
        }

        setOnClickListeners(binding.tvUserLoginTips,binding.tvUserBarLoginTips) {
            startActivity(
                Intent(
                    this,
                    LoginOKActivity::class.java
                )
            )
        }
        //防止点击事件被未消费，导致列表被点击
        binding.blockUserBar.setOnClickListener {  }
        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            //region 用戶積分Bar
            /**
             * 用戶積分區塊高度(118dp)滾動後剩餘高度
             */
            val userBlockOffset = 118.dp - abs(verticalOffset)

            /**
             * 用戶積分Bar是否出現
             * 用戶積分Bar由距離自身兩倍高度的位置開始顯示, 由下方offset慢慢向下推移
             */
            val showUserBarBlock = userBlockOffset < (56.dp * 2)
            binding.blockUserBar.isVisible = showUserBarBlock

            /**
             * 用戶積分Bar位移量
             * 最多只位移至原位
             */
            val userBarOffset = min(0, 56.dp - userBlockOffset)
            binding.blockUserBar.translationY = userBarOffset.toFloat()
            //endregion 用戶積分Bar

            binding.toolBar.setToolbarBackgroundColor(
                if (verticalOffset == 0) {
                    R.color.transparent
                } else {
                    R.color.color_F6F7F8
                }
            )
        })

        binding.btnPointHistory.setOnClickListener {
            loginedRun(this, true) {
                startActivity(Intent(this, PointHistoryActivity::class.java))
            }
        }

        binding.btnExchangeHistory.setOnClickListener {
            loginedRun(this, true) {
                startActivity(Intent(this, PointExchangeActivity::class.java))
            }
        }

        //商品類別
        initShopTypeRecyclerView()

        //region 商品類別篩選器
        binding.tabFilterRecommend.setOnClickListener {
            viewModel.selectShopTypeFilterTab(ShopTypeFilterType.RECOMMEND)
        }
        binding.tabFilterCanBuy.setOnClickListener {
            viewModel.selectShopTypeFilterTab(ShopTypeFilterType.CAN_BUY)
        }
        binding.viewFilterPrices.setOnClickListener {
            viewModel.selectShopTypeFilterTab(ShopTypeFilterType.PRICES)
        }
        //endregion 商品類別篩選器

        //商品清單
        initShopGoodsRecyclerView()

    }

    private fun initObservable() {
        viewModel.isLogin.observe(this) {
            //region 已登入
            binding.ivUserPoint.isVisible = it
            binding.tvUserBarPoint.isVisible = it
            binding.linUserPointWarning.isVisible = it
            binding.ivUserBarPoint.isVisible = it
            binding.tvUserPoint.isVisible = it
            //endregion 已登入

            //region 未登入
            binding.tvUserLoginTips.isVisible = !it
            binding.tvUserBarLoginTips.isVisible = !it
            //endregion 未登入
        }

        PointRepository.userPoint.collectWith(lifecycleScope) {
            binding.tvUserPoint.text = TextUtil.formatMoney2(it ?: 0)
            binding.tvUserBarPoint.text = TextUtil.formatMoney2(it ?: 0)
        }

        viewModel.expiredPoints.collectWith(lifecycleScope) {
            if ((it?.points?.toInt()?:0)==0){
                binding.linUserPointWarning.isInvisible = true
            }else{
                binding.linUserPointWarning.isInvisible = false
                binding.tvUserPointWarning.text = getString(
                    R.string.B204,
                    TextUtil.formatMoney2(it?.points?.toInt() ?: 0),
                    TimeUtil.timeFormat(it?.expiredTime, TimeUtil.MD)
                )
            }
        }

        viewModel.announcementItem.collectWith(lifecycleScope) {
            setupAnnouncement(it)
        }

        viewModel.limitedProductItemList.collectWith(lifecycleScope) {
            setupLimitedProduct(it)
        }

        viewModel.shopMainViewEvent.collectWith(lifecycleScope) { event ->
            when (event) {
                ShopMainViewEvent.ShopTypeTabSelectedUpdate -> {
                    rvShopTypeTabAdapter.updateSelected()
                }

                is ShopMainViewEvent.ShopTypeFilterTabSelectedUpdate -> {
                    updateShopTypeFilterTab(event.tabList)
                }
            }
        }

        viewModel.shopTypeTabList.collectWith(lifecycleScope) {
            rvShopTypeTabAdapter.setList(it)
            binding.rvShopType.post {
                setupShopGoodsRecyclerViewPadding()
            }
        }

        viewModel.shopTypeFilterTabList.collectWith(lifecycleScope) {
            updateShopTypeFilterTab(it)
        }

        viewModel.shopProductItemList.collectWith(lifecycleScope) {
            rvProductsAdapter.setList(it)
        }

        viewModel.bannerImageList.collectWith(lifecycleScope) {
            if(it.isNullOrEmpty()){
                binding.cvBanner.gone()
            }else {
                binding.cvBanner.visible()
                setUpBanner(it)
            }
        }
    }

    private fun initShopTypeRecyclerView() {
        binding.rvShopType.layoutManager = ScrollCenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvShopTypeTabAdapter.setOnItemClickListener { adapter, view, position ->
            viewModel.selectShopTypeTab(rvShopTypeTabAdapter.getItem(position))
            (binding.rvShopType.layoutManager as ScrollCenterLayoutManager).scrollToPosition(position)
        }
        binding.rvShopType.adapter = rvShopTypeTabAdapter
    }

    private fun initShopGoodsRecyclerView() {
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
//        binding.rvProducts.addItemDecoration(GridSpacingItemDecoration(2,12.dp,false))
        rvProductsAdapter.setOnItemClickListener{ adapter, view, position ->
            enterDetail(rvProductsAdapter.getItem(position))
        }
        rvProductsAdapter.setEmptyView(ViewNoProductBinding.inflate(layoutInflater).root)
        binding.rvProducts.adapter = rvProductsAdapter

    }

    private fun setupShopGoodsRecyclerViewPadding() {
        binding.rvProducts.scrollToPosition(0)
    }

    private fun setUpBanner(imageList: List<Image>) {
        val loopEnable = imageList.size > 1
        if (imageList.isEmpty()) {
            return
        }
        val xBanner = binding.topBanner
        xBanner.setHandLoop(loopEnable)
        xBanner.setAutoPlayAble(loopEnable)
        xBanner.loadImage { _, model, view, _ ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
        }
        val host = sConfigData?.resServerHost
        val images = imageList.map {
            Timber.d("host:$host url1:${host + it.path}")
            XBannerImage(it.sort.toString(), host + it.path)
        }

        //opt1 ->ImageType = 5,为活动轮播图
        //opt2 ->后台有配置
        //满足以上两点 -> 显示活动轮播图r
        if (images.isNotEmpty()) {
            xBanner.visible()
        }
        xBanner.setBannerData(images.toMutableList())
    }

    //region 跑馬燈
    private fun setupAnnouncement(titleList: List<AnnouncementItem>) {
        binding.linAnnouncement.apply {
            isVisible = titleList.isNotEmpty()
        }
        val adapter = VerticalMarqueeAdapter()
        adapter.setList(titleList.map { getAnnouncementSpannable(it) })
        binding.marqueeView.setUp(adapter)
    }

    /**
     * 公告文字UI配置, 粗細體, 文字顏色
     */
    private fun getAnnouncementSpannable(announcementItem: AnnouncementItem): SpannableString {
        val userName = announcementItem.userName+" "
        val exchangeContent = " "+announcementItem.exchangeContent
        val spannableString = SpannableString(getString(R.string.A058, userName, exchangeContent))

        val nameStartIndex = spannableString.split(userName).firstOrNull()?.length ?: 0
        val nameEndIndex = nameStartIndex + userName.length

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            nameStartIndex,
            nameEndIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val exchangeStartIndex = spannableString.split(exchangeContent).firstOrNull()?.length ?: 0
        val exchangeEndIndex = exchangeStartIndex + exchangeContent.length

        spannableString.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(this, R.color.color_FF4343)
            ), exchangeStartIndex, exchangeEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }
    //endregion 跑馬燈

    //region 限時搶購
    private fun setupLimitedProduct(productList: List<Product>) {
        binding.bannerLimitedProduct.isVisible = !productList.isEmpty()

        binding.bannerLimitedProduct.setAutoPlayAble(productList.size > 1)

        binding.bannerLimitedProduct.apply {
            loadImage { _, model, view, position ->
                when (model) {
                    is Product -> {
                        val productEnable = model.storage > 0
                        val viewAlpha = if (productEnable) 1f else 0.6f
                        view.setOnClickListener {
                            if (productEnable) {
                                enterDetail(model)
                            }
                        }
                        //region 商品名稱
                        val tvProductName: TextView? = view.findViewById(R.id.tvProductName)
                        tvProductName?.text = model.name
                        tvProductName?.alpha = viewAlpha
                        //endregion 商品名稱

                        //region 商品編號
                        val tvProductIndicator: TextView? =
                            view.findViewById(R.id.tvProductIndicator)
                        tvProductIndicator?.let {
                            it.text = model.getProductIndicator(
                                this@PointShopActivity,
                                position + 1,
                                productList.size
                            )
                            it.alpha = viewAlpha
                        }
                        //endregion 商品編號

                        //region 商品倒數時間
                        val ivClock: ImageView? = view.findViewById(R.id.ivClock)
                        val cmCountDown: Chronometer? = view.findViewById(R.id.cmEndDate)

                        cmCountDown?.setMargins(
                            cmCountDown.marginStart,
                            cmCountDown.marginTop,
                            if (model.isOnSale) 35.dp else 12.dp,
                            cmCountDown.marginBottom
                        )

                        if (ivClock != null && cmCountDown != null) {
                            when{
                                System.currentTimeMillis() < model.limitSaleStart->{
                                    ivClock.visible()
                                    cmCountDown.visible()
                                    val grabStr = getString(R.string.A073)
                                    cmCountDown.text = Spanny("${TimeUtil.timeFormat(model.limitSaleStart,TimeUtil.MD_HMS)} $grabStr").
                                        findAndSpan(grabStr){ ForegroundColorSpan(ContextCompat.getColor(this@PointShopActivity,R.color.color_0D2245))
                                    }
                                }
                                System.currentTimeMillis() in model.limitSaleStart..model.limitSaleEnd->{
                                    setupTaskCountDownTimer(
                                        endDate = model.limitSaleEnd,
                                        ivClock = ivClock,
                                        cmEndDate = cmCountDown,
                                        onChronometerFinished = {
                                            viewModel.clearFinishedLimitedProduct()
                                        },
                                        showCountDownTimer = true,
                                        showBeforeTimeStarted = true,
                                        startDate = model.limitSaleStart
                                    )
                                }
                                else->{
                                    ivClock.gone()
                                    cmCountDown.stop()
                                    cmCountDown.gone()
                                }
                            }

                        }

                        ivClock?.alpha = viewAlpha
                        cmCountDown?.alpha = viewAlpha
                        //endregion 商品倒數時間

                        //region 商品折扣數
                        val tvDiscount: TextView? = view.findViewById(R.id.tvDiscount)
                        val ivDiscount: ImageView? = view.findViewById(R.id.ivDiscount)
                        model.setupProductDiscountView(tvDiscount, ivDiscount)
                        //endregion

                        //region 商品圖
                        val ivProduct: ImageView? = view.findViewById(R.id.ivProduct)
                        val tvFundValue: TextView? = view.findViewById(R.id.tvFundValue)
                        model.setupProductImage(tvFundValue, ivProduct)
                        //endregion 商品圖

                        //region 價值幣種圖 + 商品價值 + 打折前價值
                        val ivProductValueType: ImageView? =
                            view.findViewById(R.id.ivProductValueType)
                        ivProductValueType?.setImageResource(model.valueType.moneyImage)

                        val tvProductPrice: TextView? = view.findViewById(R.id.tvProductPrice)
                        tvProductPrice?.let {
                            it.text = model.getProductPrice(this@PointShopActivity)
                        }

                        ivProductValueType?.alpha = viewAlpha
                        tvProductPrice?.alpha = viewAlpha
                        //endregion 價值幣種圖 + 商品價值 + 打折前價值

                        //region 商品庫存
                        val tvStorage: TextView? = view.findViewById(R.id.tvStorage)
                        tvStorage?.text = TextUtil.formatMoney2(model.storage)

                        tvStorage?.alpha = viewAlpha
                        //endregion 商品庫存

                        //region 立即搶購按鈕
                        val btnBuy: TextView? = view.findViewById(R.id.btnBuy)

                        btnBuy?.isVisible = System.currentTimeMillis() < model.limitSaleEnd
                        btnBuy?.alpha = if(System.currentTimeMillis() > model.limitSaleStart && productEnable) 1.0f else 0.6F
                        //endregion 立即搶購按鈕

                        //region 售完蒙版
                        val viewSoldOut: View? = view.findViewById(R.id.viewSoldOut)
                        viewSoldOut?.isVisible = !productEnable
                        //endregion 售完蒙版


                    }
                }

            }
            setBannerData(R.layout.item_limited_time_product, productList)
        }
    }
    //endregion 限時搶購

    //region 商城類別排序
    private fun updateShopTypeFilterTab(shopTypeFilterTabs: List<ShopTypeFilterTabImpl>) {
        shopTypeFilterTabs.forEach { filterTab ->
            when (filterTab) {
                is ShopSortedNormalTab -> {
                    when (filterTab.tabType) {
                        ShopTypeFilterType.CAN_BUY -> {

                            filterTab.setupTextColor(binding.tabFilterCanBuy)
                        }

                        ShopTypeFilterType.RECOMMEND -> {
                            filterTab.setupTextColor(binding.tabFilterRecommend)
                        }

                        ShopTypeFilterType.PRICES -> {
                            //do nothing
                        }
                    }
                }

                is ShopSortedPricesTab -> {
                    filterTab.apply {
                        setupTextColor(binding.tabFilterPrices)
                        setupSortedIcon(binding.ivPricesAscending)
                    }
                }
            }
        }
    }

    //endregion 商城類別排序
    private fun enterDetail(product: Product) {
        if (product.type == 2) {
            ExchangeGoodsDialog.newInstance(product).show(supportFragmentManager)
        } else {
            ExchangeRewardDialog.newInstance(product).show(supportFragmentManager)
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateMall(event: UpdatePointEvent){
        viewModel.getProductIndex()
    }
}