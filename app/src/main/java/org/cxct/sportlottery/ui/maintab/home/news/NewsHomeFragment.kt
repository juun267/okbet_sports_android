package org.cxct.sportlottery.ui.maintab.home.news

import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentNewsHomeBinding
import org.cxct.sportlottery.net.PageInfo
import org.cxct.sportlottery.net.news.NewsRepository
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter
import timber.log.Timber

class NewsHomeFragment : BaseSocketFragment<MainHomeViewModel, FragmentNewsHomeBinding>() {


    private inline fun getMainTabActivity() = activity as MainTabActivity
    private val PAGE_SIZE = 4
    private var currentPage = 1;

    override fun onInitView(view: View) {
        initToolBar()
        initRecyclerView()
        initNews()
        setUpBanner()
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        initObservable()
        viewModel.getNewsCategory()
//        if (currentPage == 1) { // 第一次进来的时候
//            viewModel.getPageNews(currentPage, PAGE_SIZE, categoryId)
//        }
    }

    fun initToolBar() = binding.homeToolbar.run {
        attach(this@NewsHomeFragment, getMainTabActivity(), viewModel)
        tvUserMoney.setOnClickListener {
            EventBusUtil.post(MenuEvent(true, Gravity.RIGHT))
            getMainTabActivity().showMainRightMenu()
        }
    }

    private fun initRecyclerView() = binding.includeNews.rvNews.run {
        layoutManager = setLinearLayoutManager()
        adapter = HomeNewsAdapter().apply {
            setOnItemClickListener { adapter, _, position ->
                NewsDetailActivity.start(requireContext(),(adapter.data[position] as NewsItem))
            }
        }
    }

    private fun initObservable() {
        viewModel.pageNewsList.observe(this) {
            currentPage = it.pageNum
            setupNews(it.pageNum, it.records ?: listOf())
            setupShowMore(it)
        }
        viewModel.newsCategory.observe(this){
            binding.includeNews.apply {
                tabNews.removeAllTabs()
                it.forEach {
                    tabNews.addTab(tabNews.newTab().setTag(it.id).setText(it.categoryName))
                }
                tabNews.getTabAt(0)?.select()
                getSelectCategoryId()?.let {
                    viewModel.getPageNews(1, PAGE_SIZE, it)
                }
            }
        }
    }
    private fun initNews()=binding.includeNews.run {
        tvCateName.text = getString(R.string.N912)
        tvMore.gone()
//            ivMore.gone()
        tabNews.addOnTabSelectedListener(TabSelectedAdapter { tab, _ ->
            getSelectCategoryId()?.let {
                viewModel.getPageNews(1, PAGE_SIZE, it)
            }
        })
        binding.tvShowMore.setOnClickListener {
            getSelectCategoryId()?.let {
                viewModel.getPageNews(currentPage + 1, PAGE_SIZE, it)
            }
        }
        binding.bottomView.bindServiceClick(childFragmentManager)
    }

    private fun setupNews(pageNum: Int, newsList: List<NewsItem>) {
        (binding.includeNews.rvNews.adapter as HomeNewsAdapter).apply {
            if (pageNum > 1) {
                addData(newsList)
            } else {
                setList(newsList)
            }
        }
    }
    private fun getSelectCategoryId():Int?=binding.includeNews.tabNews.getTabAt(binding.includeNews.tabNews.selectedTabPosition)?.tag as? Int

    private fun setupShowMore(pageInfo: PageInfo<NewsItem>) {
        binding.tvShowMore.apply {
            isVisible = pageInfo.pageNum < pageInfo.totalNum
            if (isVisible) {
                text =
                    "${getString(R.string.N885)} (${pageInfo.totalSize - pageInfo.pageNum * PAGE_SIZE})"
            }
        }
    }
    private fun setUpBanner() {
        val lang = LanguageManager.getSelectLanguage(context).key
        var imageList = sConfigData?.imageList?.filter {
            it.imageType == ImageType.BANNER_NEWS.code && it.lang == lang && !it.imageName1.isNullOrEmpty() && (!getMarketSwitch() && !it.isHidden)
        }?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })
        val loopEnable = (imageList?.size ?: 0) > 1
        if (imageList.isNullOrEmpty()) {
            binding.ivCover.visible()
            binding.clBanner.gone()
            return
        }else{
            binding.ivCover.gone()
            binding.clBanner.visible()
        }
        binding.xbanner.setHandLoop(loopEnable)
        binding.xbanner.setOnItemClickListener { banner, model, view, position ->
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
        binding.xbanner.loadImage { _, model, view, _ ->
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
            binding.xbanner.visible()
        }
        binding.xbanner.setBannerData(images.toMutableList())
    }
}