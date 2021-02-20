package org.cxct.sportlottery.ui.home2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.stx.xhb.xbanner.XBanner
import kotlinx.android.synthetic.main.fragment_home_start_page.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.JumpUtil

class HomeStartPageFragment : BaseFragment<MainViewModel>(MainViewModel::class) {

    private val mMarqueeAdapter = MarqueeAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_start_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initBannerAndPopImage()
        initRvMarquee()
        initObserve()
        getAnnouncement()

        initTab()
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()
    }

    //輪播廣告圖示
    private fun setBanner(bannerList: List<ImageData>) {
        //如果有 連結url, 點擊跳轉畫面
        xBanner.setOnItemClickListener { banner: XBanner, model: Any, view: View, position: Int ->
            if (!bannerList[position].imageLink.isNullOrEmpty())
                JumpUtil.toExternalWeb(xBanner.context, bannerList[position].imageLink)
        }

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_image_load)
            .error(R.drawable.ic_image_broken)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontTransform()

        //加載圖片
        xBanner.loadImage { xBanner: XBanner, model: Any, view: View, position: Int ->
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

        xBanner.setData(bannerList, null)
    }

    //彈窗圖
    private fun setPopImage(popImageList: List<ImageData>) {
        context?.run {
            PopImageDialog(this, popImageList).show()
        }
    }

    //獲取 輪播圖 和 彈窗圖
    private fun initBannerAndPopImage() {
        val bannerList = mutableListOf<ImageData>()
        val popImageList = mutableListOf<ImageData>()
        sConfigData?.imageList?.forEach { data ->
            when (data.imageType) {
                2 -> { //H5轮播
                    bannerList.add(data)
                }
                7 -> { //H5彈窗圖
                    popImageList.add(data)
                }
            }
        }

        //H5轮播
        if (!bannerList.isNullOrEmpty()) {
            setBanner(bannerList.sortedBy { it.imageSort })
        }

        //彈窗圖
        if (!popImageList.isNullOrEmpty()) {
            setPopImage(popImageList.sortedBy { it.imageSort })
        }
    }

    //公告
    private fun initRvMarquee() {
        rv_marquee.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rv_marquee.adapter = mMarqueeAdapter
    }

    private fun initObserve() {
        viewModel.messageListResult.observe(viewLifecycleOwner, Observer {
            hideLoading()
            updateUiWithResult(it)
        })
    }

    private fun updateUiWithResult(messageListResult: MessageListResult) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult.rows?.forEach { data -> titleList.add(data.title + " - " + data.message) }

        if (messageListResult.success && titleList.size > 0) {
            rv_marquee.startAuto() //啟動跑馬燈
        } else {
            rv_marquee.stopAuto() //停止跑馬燈
        }

        mMarqueeAdapter.setData(titleList)
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
    }

    private fun initTab() {
        tab_sport.setOnClickListener {
            selectTab(tab_sport)
        }

        tab_lottery.setOnClickListener {
            selectTab(tab_lottery)
        }

        tab_live.setOnClickListener {
            selectTab(tab_live)
        }

        tab_poker.setOnClickListener {
            selectTab(tab_poker)
        }

        tab_slot.setOnClickListener {
            selectTab(tab_slot)
        }

        tab_fishing.setOnClickListener {
            selectTab(tab_fishing)
        }

    }

    private fun selectTab(select: View) {
        tab_sport.isSelected = tab_sport == select
        tab_lottery.isSelected = tab_lottery == select
        tab_live.isSelected = tab_live == select
        tab_poker.isSelected = tab_poker == select
        tab_slot.isSelected = tab_slot == select
        tab_fishing.isSelected = tab_fishing == select
    }
}
