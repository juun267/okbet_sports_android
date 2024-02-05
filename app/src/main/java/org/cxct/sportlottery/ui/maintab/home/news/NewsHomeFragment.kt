package org.cxct.sportlottery.ui.maintab.home.news

import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.include_home_news.*
import kotlinx.android.synthetic.main.view_home_news.rvNews
import kotlinx.android.synthetic.main.view_home_news.tvCateName
import kotlinx.android.synthetic.main.view_home_news.tvMore
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
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.getMarketSwitch
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter
import timber.log.Timber

class NewsHomeFragment : BaseSocketFragment<MainHomeViewModel, FragmentNewsHomeBinding>() {

    private var categoryId = NewsRepository.NEWS_OKBET_ID

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private val PAGE_SIZE = 4
    private var currentPage = 1;

    override fun onInitView(view: View) {
        initToolBar()
        initRecyclerView()
        initNews()
        setUpBanner()
    }

    override fun onInitData() {
        initObservable()
        if (currentPage == 1) { // 第一次进来的时候
            viewModel.getPageNews(currentPage, PAGE_SIZE, categoryId)
        }
    }

    fun initToolBar() = binding.homeToolbar.run {
        attach(this@NewsHomeFragment, getMainTabActivity(), viewModel)
        tvUserMoney.setOnClickListener {
            EventBusUtil.post(MenuEvent(true, Gravity.RIGHT))
            getMainTabActivity().showMainRightMenu()
        }
    }

    private fun initRecyclerView() = rvNews.run {
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
    }
    private fun initNews() {
        tvCateName.text = getString(R.string.N912)
        tvMore.gone()
//            ivMore.gone()
        tabNews.addOnTabSelectedListener(TabSelectedAdapter { tab, _ ->
            categoryId = if (tab.position == 0) NewsRepository.NEWS_OKBET_ID else NewsRepository.NEWS_SPORT_ID
            viewModel.getPageNews(1, PAGE_SIZE, categoryId)
        })
        binding.tvShowMore.setOnClickListener {
            viewModel.getPageNews(currentPage + 1, PAGE_SIZE, categoryId)
        }
        binding.bottomView.bindServiceClick(childFragmentManager)
    }

    private fun setupNews(pageNum: Int, newsList: List<NewsItem>) {
        (rvNews.adapter as HomeNewsAdapter).apply {
            if (pageNum > 1) {
                addData(newsList)
            } else {
                setList(newsList)
            }
        }
    }

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