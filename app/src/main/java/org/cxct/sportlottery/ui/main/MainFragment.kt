package org.cxct.sportlottery.ui.main


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.stx.xhb.xbanner.XBanner
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.view_message.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.GameItemData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.startRegister


class MainFragment : BaseFragment<MainViewModel>(MainViewModel::class) {

    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?> = object : OnSelectItemListener<ThirdDictValues?> {
        override fun onClick(select: ThirdDictValues?) {
            loading()
            viewModel.requestEnterThirdGame(select)
        }
    }

    //電子遊戲 點擊事件特別處理
    private var mOnSelectThirdGameDzListener: OnSelectItemListener<ThirdDictValues?> = object : OnSelectItemListener<ThirdDictValues?> {
        override fun onClick(select: ThirdDictValues?) {
            goToMainMoreFragment(select?.gameCategory, select?.firmCode)
        }
    }

    private var mLastAction = Action.IS_TAB_SELECT

    private enum class Action { IS_SCROLL, IS_TAB_SELECT }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getBanner()
        getThirdGame()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initTab()
            initScrollView()
            initObserve()

            setupSport()
            setMoreButtons()
            setupUpdate()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()
    }

    private fun initTab() {
        tab_sport.setOnClickListener {
            selectTab(tab_sport)
            scrollToTabPosition(tab_sport)
            appbar_layout.setExpanded(false)
        }

        tab_lottery.setOnClickListener {
            selectTab(tab_lottery)
            scrollToTabPosition(tab_lottery)
            appbar_layout.setExpanded(false)
        }

        tab_live.setOnClickListener {
            selectTab(tab_live)
            scrollToTabPosition(tab_live)
            appbar_layout.setExpanded(false)
        }

        tab_poker.setOnClickListener {
            selectTab(tab_poker)
            scrollToTabPosition(tab_poker)
            appbar_layout.setExpanded(false)
        }

        tab_slot.setOnClickListener {
            selectTab(tab_slot)
            scrollToTabPosition(tab_slot)
            appbar_layout.setExpanded(false)
        }

        tab_fishing.setOnClickListener {
            selectTab(tab_fishing)
            scrollToTabPosition(tab_fishing)
            appbar_layout.setExpanded(false)
        }

        selectTab(tab_sport) //default select
    }

    private fun selectTab(select: View) {
        tab_sport.isSelected = tab_sport == select
        tab_lottery.isSelected = tab_lottery == select
        tab_live.isSelected = tab_live == select
        tab_poker.isSelected = tab_poker == select
        tab_slot.isSelected = tab_slot == select
        tab_fishing.isSelected = tab_fishing == select
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initScrollView() {
        scroll_view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> mLastAction = Action.IS_SCROLL
            }
            false // Do not consume events
        }

        scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (mLastAction == Action.IS_SCROLL) {
                when {
                    label_sport.isVisible && (scrollY in 0 until btn_sport.bottom) -> {
                        selectTab(tab_sport)
                    }
                    label_lottery.isVisible && scrollY in getPreGamePagerBottom(lotteryGamePager) until lotteryGamePager.bottom -> {
                        selectTab(tab_lottery)
                    }
                    label_live.isVisible && scrollY in getPreGamePagerBottom(liveGamePager) until liveGamePager.bottom -> {
                        selectTab(tab_live)
                    }
                    label_poker.isVisible && scrollY in getPreGamePagerBottom(pokerGamePager) until pokerGamePager.bottom -> {
                        selectTab(tab_poker)
                    }
                    label_slot.isVisible && scrollY in getPreGamePagerBottom(slotGamePager) until slotGamePager.bottom -> {
                        selectTab(tab_slot)
                    }
                    label_fishing.isVisible && scrollY in getPreGamePagerBottom(fishingGamePager) until fishingGamePager.bottom -> {
                        selectTab(tab_fishing)
                    }
                }
            }
        })

        //為了讓滑動切換 tab 效果能到最後一項，需動態變更 over_scroll_view 高度，來補足滑動距離
        scroll_view.post {
            try {
                val distanceY = view_footer.bottom - getLastLabel().top
                val paddingHeight = scroll_view.height - distanceY

                if (paddingHeight > 0)
                    view_footer.minimumHeight = paddingHeight
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 獲取前一個有顯示的遊戲項目Bottom
     */
    private fun getPreGamePagerBottom(gamePager: MainGamePager): Int {
        val childViewList =
            listOf<View>(btn_sport, lotteryGamePager, liveGamePager, pokerGamePager, slotGamePager, fishingGamePager)
        if (childViewList.indexOf(gamePager) < 0) return 0
        val targetViewList = childViewList.subList(0, childViewList.indexOf(gamePager))

        return targetViewList.last { it.isVisible }.bottom
    }

    /**
     * 獲取最後一個有顯示的遊戲Label
     */
    private fun getLastLabel(): MainLabel {
        return listOf<MainLabel>(
            label_fishing,
            label_slot,
            label_poker,
            label_live,
            label_lottery,
            label_sport
        ).first { it.isVisible }
    }

    private fun scrollToTabPosition(tab: View) {
        mLastAction = Action.IS_TAB_SELECT
        when (tab) {
            tab_sport -> {
                scroll_view.smoothScrollTo(0, label_sport.top)
            }
            tab_lottery -> {
                scroll_view.smoothScrollTo(0, label_lottery.top)
            }
            tab_live -> {
                scroll_view.smoothScrollTo(0, label_live.top)
            }
            tab_poker -> {
                scroll_view.smoothScrollTo(0, label_poker.top)
            }
            tab_slot -> {
                scroll_view.smoothScrollTo(0, label_slot.top)
            }
            tab_fishing -> {
                scroll_view.smoothScrollTo(0, label_fishing.top)
            }
        }
    }

    private fun initObserve() {
        //輪播圖
        viewModel.bannerList.observe(viewLifecycleOwner, {
            setBanner(it?: listOf())
        })

        //公告跑馬燈
        viewModel.promoteNoticeResult.observe(viewLifecycleOwner, {
            it?.peekContent()?.let { result ->
                setMarquee(result)
            }
        })

        //第三方遊戲清單
        viewModel.gameCateDataList.observe(viewLifecycleOwner, {
            setGameData(it)
        })

        viewModel.enterThirdGameResult.observe(viewLifecycleOwner, {
            if (isVisible)
                enterThirdGame(it)
        })
    }

    //輪播廣告圖示
    private fun setBanner(bannerList: List<ImageData>) {
        //如果有 連結url, 點擊跳轉畫面
        xBanner.setOnItemClickListener { _: XBanner, _: Any, _: View, position: Int ->
            if (!bannerList[position].imageLink.isNullOrEmpty())
                JumpUtil.toExternalWeb(xBanner.context, bannerList[position].imageLink)
        }

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_image_load)
            .error(R.drawable.ic_image_broken)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontTransform()

        //加載圖片
        xBanner.loadImage { _: XBanner, _: Any, view: View, position: Int ->
            try {
                (view as ImageView).scaleType = ImageView.ScaleType.CENTER_CROP

                //1、此处使用的Glide加载图片，可自行替换自己项目中的图片加载框架
                //2、返回的图片路径为Object类型，你只需要强转成你传输的类型就行，切记不要胡乱强转！
                val url = sConfigData?.resServerHost + bannerList[position].imageName1
                if (url.endsWith(".gif")) { //判斷是否為 gif 圖片
                    Glide.with(this)
                        .asGif()
                        .load(url)
                        .apply(requestOptions)
                        .into(view)
                } else {
                    Glide.with(this)
                        .load(url)
                        .apply(requestOptions)
                        .into(view)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (bannerList.size <= 1) {
            banner_arrow_l.visibility = View.GONE
            banner_arrow_r.visibility = View.GONE
            xBanner.setAutoPlayAble(false)
        } else {
            banner_arrow_l.visibility = View.VISIBLE
            banner_arrow_r.visibility = View.VISIBLE
            xBanner.setAutoPlayAble(true)
        }
        xBanner.setData(bannerList, null)
    }

    //公告跑馬燈
    private fun setMarquee(messageListResult: MessageListResult) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult.rows?.forEach { data -> titleList.add(data.title + " - " + data.message) }

        val adapter = MarqueeAdapter()
        adapter.setData(titleList)
        rv_marquee.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rv_marquee.adapter = adapter

        if (messageListResult.success && titleList.size > 0) {
            rv_marquee.startAuto() //啟動跑馬燈
        } else {
            rv_marquee.stopAuto() //停止跑馬燈
        }
    }

    private fun setGameData(cateDataList: List<GameCateData>?) {
        //第三方遊戲開啟才顯示 類別 tabLayout
        tab_layout.visibility = if (sConfigData?.thirdOpen == FLAG_OPEN) View.VISIBLE else View.GONE

        refreshGameCGCP(cateDataList?.find { it.categoryThird == ThirdGameCategory.CGCP })
        refreshGameLive(cateDataList?.find { it.categoryThird == ThirdGameCategory.LIVE })
        refreshGameQP(cateDataList?.find { it.categoryThird == ThirdGameCategory.QP })
        refreshGameDZ(cateDataList?.find { it.categoryThird == ThirdGameCategory.DZ })
        refreshGameBY(cateDataList?.find { it.categoryThird == ThirdGameCategory.BY })
    }

    private fun enterThirdGame(result: EnterThirdGameResult) {
        hideLoading()
        when (result.resultType) {
            EnterThirdGameResult.ResultType.SUCCESS -> context?.run {
                JumpUtil.toThirdGameWeb(this,
                    result.url ?: "")
            }
            EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(getString(R.string.error),
                result.errorMsg ?: "") {}
            EnterThirdGameResult.ResultType.NEED_REGISTER -> requireActivity().startRegister()
            EnterThirdGameResult.ResultType.GUEST -> showErrorPromptDialog(getString(R.string.error),
                result.errorMsg ?: "") {}
            EnterThirdGameResult.ResultType.NONE -> {
            }
        }
        if (result.resultType != EnterThirdGameResult.ResultType.NONE)
            viewModel.clearThirdGame()
    }

    //彩票
    private fun refreshGameCGCP(cateData: GameCateData?) {
        val gameList = mutableListOf<GameItemData>()

        cateData?.tabDataList?.forEach {
            it.gameList.run { gameList.addAll(this) }
        }

        if (gameList.isEmpty()) {
            tab_lottery.visibility = View.GONE
            label_lottery.visibility = View.GONE
            lotteryGamePager.visibility = View.GONE
        } else {
            tab_lottery.visibility = View.VISIBLE
            label_lottery.visibility = View.VISIBLE
            lotteryGamePager.visibility = View.VISIBLE

            lotteryGamePager.isShowArrow(false)
            lotteryGamePager.enableItemLoop(false)
            lotteryGamePager.setOnSelectThirdGameListener(mOnSelectThirdGameListener)
            lotteryGamePager.setData(gameList)
        }
    }

    //真人
    private fun refreshGameLive(cateData: GameCateData?) {
        val gameList = mutableListOf<GameItemData>()

        cateData?.tabDataList?.forEach {
            it.gameList.run { gameList.addAll(this) }
        }

        if (gameList.isEmpty()) {
            tab_live.visibility = View.GONE
            label_live.visibility = View.GONE
            liveGamePager.visibility = View.GONE
        } else {
            tab_live.visibility = View.VISIBLE
            label_live.visibility = View.VISIBLE
            liveGamePager.visibility = View.VISIBLE

            liveGamePager.isShowArrow(gameList.size > 3)
            liveGamePager.enableItemLoop(gameList.size > 3)
            liveGamePager.setOnSelectThirdGameListener(mOnSelectThirdGameListener)
            liveGamePager.setData(gameList)
        }
    }

    //棋牌
    private fun refreshGameQP(cateData: GameCateData?) {
        val gameList = mutableListOf<GameItemData>()

        cateData?.tabDataList?.forEach {
            it.gameList.run { gameList.addAll(this) }
        }

        if (gameList.isEmpty()) {
            tab_poker.visibility = View.GONE
            label_poker.visibility = View.GONE
            pokerGamePager.visibility = View.GONE
        } else {
            tab_poker.visibility = View.VISIBLE
            label_poker.visibility = View.VISIBLE
            pokerGamePager.visibility = View.VISIBLE

            pokerGamePager.isShowArrow(gameList.size > 1)
            pokerGamePager.enableItemLoop(gameList.size > 1)
            pokerGamePager.setOnSelectThirdGameListener(mOnSelectThirdGameListener)
            pokerGamePager.setData(gameList)
        }
    }

    //電子 //電子遊戲特別處理，此處展示的是第二層 tab，點擊要跳轉到對應遊戲列表畫面
    private fun refreshGameDZ(cateData: GameCateData?) {
        val gameList = mutableListOf<GameItemData>()

        cateData?.tabDataList?.forEach {
            if (it.gameFirm != null) {
                val tab = viewModel.createSingleThirdGame(it.gameCategory, it.gameFirm)
                gameList.add(tab)
            }
        }

        if (gameList.isEmpty()) {
            tab_slot.visibility = View.GONE
            label_slot.visibility = View.GONE
            slotGamePager.visibility = View.GONE
        } else {
            tab_slot.visibility = View.VISIBLE
            label_slot.visibility = View.VISIBLE
            slotGamePager.visibility = View.VISIBLE

            slotGamePager.isShowArrow(gameList.size > 2)
            slotGamePager.enableItemLoop(gameList.size > 2)
            slotGamePager.setOnSelectThirdGameListener(mOnSelectThirdGameDzListener)
            slotGamePager.setData(gameList)
        }
    }

    //捕魚
    private fun refreshGameBY(cateData: GameCateData?) {
        val gameList = mutableListOf<GameItemData>()

        cateData?.tabDataList?.forEach {
            it.gameList.run { gameList.addAll(this) }
        }

        if (gameList.isEmpty()) {
            tab_fishing.visibility = View.GONE
            label_fishing.visibility = View.GONE
            fishingGamePager.visibility = View.GONE
        } else {
            tab_fishing.visibility = View.VISIBLE
            label_fishing.visibility = View.VISIBLE
            fishingGamePager.visibility = View.VISIBLE

            fishingGamePager.enableItemLoop(gameList.size > 2)
            fishingGamePager.setOnSelectThirdGameListener(mOnSelectThirdGameListener)
            fishingGamePager.setData(gameList)
        }
    }

    private fun getBanner() {
        viewModel.getBanner()
    }

    private fun getThirdGame() {
        viewModel.getThirdGame()
    }

    private fun setupSport() {
        btn_sport.setOnClickListener {
            GamePublicityActivity.reStart(context ?: requireActivity())
        }
    }

    private fun setMoreButtons() {
        label_live.setOnMoreClickListener {
            goToMainMoreFragment(ThirdGameCategory.LIVE.name)
        }

        label_poker.setOnMoreClickListener {
            goToMainMoreFragment(ThirdGameCategory.QP.name)
        }

        label_slot.setOnMoreClickListener {
            goToMainMoreFragment(ThirdGameCategory.DZ.name)
        }

        label_fishing.setOnMoreClickListener {
            goToMainMoreFragment(ThirdGameCategory.BY.name)
        }
    }

    private fun setupUpdate() {
        btn_update.apply {
            setOnClickListener {
                startActivity(Intent(activity, VersionUpdateActivity::class.java))
            }
        }
    }

    private fun goToMainMoreFragment(cateCode: String?, firmCode: String? = null) {
        val action = MainFragmentDirections.actionMainFragmentToMainMoreFragment(cateCode ?: "", firmCode ?: "")
        findNavController().navigate(action)
    }
}
