package org.cxct.sportlottery.ui.maintab.home


import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentHome2Binding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.ui.maintab.home.news.NewsHomeFragment
import org.cxct.sportlottery.ui.maintab.home.view.HomeMenuAdapter
import org.cxct.sportlottery.ui.maintab.publicity.MarqueeAdapter
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import timber.log.Timber

class HomeFragment2 : BindingFragment<MainHomeViewModel,FragmentHome2Binding>(){
    private fun getMainTabActivity() = activity as MainTabActivity
//    private val fragmentHelper by lazy {
//        FragmentHelper(childFragmentManager, binding.flContent.id, arrayOf(
//            Param(HomeHotFragment::class.java),
//            Param(NewsHomeFragment::class.java, needRemove = true),
//            Param(OKLiveFragment::class.java, needRemove = true),
//            Param(GameVenueFragment::class.java, needRemove = true),
//        ))
//    }
    private val fragmentHelper2 by lazy { FragmentHelper2(childFragmentManager, R.id.flContent) }
    private lateinit var hotFragment: HomeHotFragment
    private val homeMenuAdapter by lazy {
        HomeMenuAdapter { view, item->
            item.third?.let {
                fragmentHelper2.show(it) { fragment, _ ->

                }
            }
        }
    }

    override fun onInitView(view: View) {
        initToolBar()
        binding.rvMarquee.bindLifecycler(this)
        initMenu()
        initIndicate()

    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        initObservable()
        viewModel.getConfigData()
        viewModel.getAnnouncement()
    }



    private fun initObservable() {
        ConfigRepository.onNewConfig(this) {
            setUpBanner()
            viewModel.getActivityImageListH5()
        }
        //新版宣傳頁
        viewModel.messageListResult.observe(viewLifecycleOwner) {

            val messageListResult = it.getContentIfNotHandled() ?: return@observe
            val sortMsgList =
                messageListResult.rows?.sortedWith(compareByDescending<Row> { it.sort }.thenByDescending { it.addTime })
            val titleList: MutableList<String> = mutableListOf()
            sortMsgList?.forEach { data ->
                if (data.type.toInt() == 1) {
                    titleList.add(data.title + " - " + data.message)
                }
            }
            setupAnnouncement(titleList)
        }
    }

    private fun initToolBar() = binding.run {
        homeToolbar.attach(this@HomeFragment2, getMainTabActivity(), viewModel)
        homeToolbar.ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            getMainTabActivity().showMainLeftMenu(null)
        }
        homeToolbar.tvUserMoney.setOnClickListener {
            EventBusUtil.post(MenuEvent(true,Gravity.RIGHT))
            getMainTabActivity().showMainRightMenu()
        }
    }
    private fun setUpBanner() {
        val imageType = 2
        val lang = LanguageManager.getSelectLanguage(context).key
        var imageList = sConfigData?.imageList?.filter {
            it.imageType == imageType && it.lang == lang && !it.imageName1.isNullOrEmpty() && (!getMarketSwitch() && !it.isHidden)
        }?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })
        val loopEnable = (imageList?.size ?: 0) > 1
        if (imageList.isNullOrEmpty()) {
            return
        }
        var xbanner = binding.topBanner
        xbanner.setHandLoop(loopEnable)
        xbanner.setOnItemClickListener { banner, model, view, position ->
            val jumpUrl = (model as XBannerImage).jumpUrl
            if (jumpUrl.isNullOrEmpty()) {
                return@setOnItemClickListener
            }
            if (jumpUrl!!.contains("sweepstakes")) {
                JumpUtil.toLottery(requireContext(), Constants.getLotteryH5Url(requireContext(), LoginRepository.token))
            } else {
                JumpUtil.toInternalWeb(requireContext(), jumpUrl, "")
            }
        }
        xbanner.loadImage { _, model, view, _ ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
        }
        val host = sConfigData?.resServerHost
        val images = imageList.map {
            Timber.d("host:$host url1:${host + it.imageName1}")
            XBannerImage(it.imageText1 + "", host + it.imageName1, it.appUrl)
        }
        //opt1 ->ImageType = 5,为活动轮播图
        //opt2 ->后台有配置
        //满足以上两点 -> 显示活动轮播图r
        if (images.isNotEmpty()) {
            xbanner.visible()
        }
        xbanner.setBannerData(images.toMutableList())
    }

    private fun setupAnnouncement(titleList: List<String>) = binding.linAnnouncement.run {
        if (titleList.isEmpty()) {
            visibility = View.GONE
            return@run
        }

        visibility = View.VISIBLE
        var marqueeAdapter = MarqueeAdapter()
        setOnClickListener {
            startActivity(Intent(requireActivity(), NewsActivity::class.java))
        }
        val rvMarquee = binding.rvMarquee
        rvMarquee.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvMarquee.adapter = marqueeAdapter
        marqueeAdapter.setData(titleList.toMutableList())
        if (titleList.isNotEmpty()) {
            rvMarquee.startAuto(false) //啟動跑馬燈
        } else {
            rvMarquee.stopAuto(true) //停止跑馬燈
        }
    }

    private fun initMenu() = binding.rvMenu.run {
        layoutManager = LinearLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false)
        adapter = homeMenuAdapter
        fragmentHelper2.show(HomeHotFragment::class.java) { frament, _ ->
            hotFragment = frament
        }
//       PagerSnapHelper().attachToRecyclerView(this)
    }

    private fun initIndicate(){
        binding.hIndicator.run {
            setIndicatorColor(context.getColor(R.color.color_E0E3EE), context.getColor(R.color.color_025BE8))
            val height = 6.dp
            itemWidth = 12.dp
            itemHeight = height
            mRadius = itemWidth.toFloat()
            setSpacing(height)
            itemClickListener = { binding.rvMenu.smoothScrollToPosition(it) }
        }

        binding.rvMenu.doOnLayout {
            binding.rvMenu.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                val itemWidth = binding.rvMenu.measuredWidth.toFloat() // 这个很重要，item的宽度要刚好等于recyclerview的宽度，不然PagerSnapHelper翻页会存在滑动偏差导致指示器位置计算的不准
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val scrollX = recyclerView.computeHorizontalScrollOffset()
                    val positionFloat = scrollX / itemWidth
                    val position = positionFloat.toInt()
                    val progress = positionFloat - position
                    binding.hIndicator.onPageScrolled(position, progress, scrollX)
                }
            })
        }
    }


    fun backMainHome() {
        fragmentHelper2.show(HomeHotFragment::class.java) { frgment, _ ->
            hotFragment = frgment
        }
    }


    fun jumpToNews() {}

    fun jumpToOKLive() { }


    fun jumpToInplaySport() {
        (activity as MainTabActivity).jumpToInplaySport()
    }

    fun jumpToDefaultSport() {
        (activity as MainTabActivity).jumpToTheSport()
    }

    fun jumpToEarlySport() {
        (activity as MainTabActivity).jumpToEarlySport()
    }

//    override fun onHiddenChanged(hidden: Boolean) {
//        super.onHiddenChanged(hidden)
//        fragmentHelper.getFragmentList().find {
//            it != null && it.isAdded && it.isVisible
//        }?.let {
//            it.onHiddenChanged(hidden)
//        }
//    }

    fun getCurrentFragment() = fragmentHelper2.currentFragment()


    override fun onResume() {
        super.onResume()
        viewModel.getConfigData()
    }

}