package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ViewGamePageBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.view.onClick

class GamesPageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    private val binding: ViewGamePageBinding

    //点击更多
    private var onMoreClick: () -> Unit = {}

    //点击游戏
    private var onGameClick: (data: OKGameBean) -> Unit = {}

    //收藏点击
    private var onFavoriteClick: (item: OKGameBean) -> Unit = {}

    //游戏列表数据
    private val dataList = arrayListOf<List<OKGameBean>>()
    private val mAdapter = RecyclerGamePageAdapter()
    private val pageSize = 6

    init {
        orientation = VERTICAL
        binding = ViewGamePageBinding.inflate(LayoutInflater.from(context), this, true)
        initView()
    }


    private fun initView() {
        binding.run {
            PagerSnapHelper().attachToRecyclerView(rvGameItem)
            val manager = LinearLayoutManager(context)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            rvGameItem.layoutManager = manager
            rvGameItem.adapter = mAdapter
            rvGameItem.itemAnimator?.changeDuration = 0
            //上一页
            ivBackPage.onClick {
                scrollRecycler(false)
            }
            //下一页
            ivForwardPage.onClick {
                scrollRecycler(true)
            }
            //游戏点击
            mAdapter.setOnGameClick {
                onGameClick(it)
            }
            //收藏点击
            mAdapter.setOnFavoriteClick {
                onFavoriteClick(it)
//                it.markCollect=!it.markCollect
//                mAdapter.notifyDataSetChanged()
            }
            mAdapter.setOnJumpToMore {
                onMoreClick()
            }
            //更多点击
            tvMore.onClick {
                onMoreClick()
            }

            setRecyclerScrollListener()
        }
    }

    //设置数据
    fun setListData(data: List<OKGameBean>?): GamesPageView {
        dataList.clear()
        if (data == null) {
            return this
        }
        //最多显示18个
        val cutData: List<OKGameBean> =
            if (data.size > 18) {
                mAdapter.setIsMoreThan(true)
                data.subList(0, 18)
            } else {
                mAdapter.setIsMoreThan(false)
                data
            }
        //填充数据
        groupDataList(cutData)

        //是否显示更多
        setViewVisible(cutData.size > pageSize)
        return this
    }


    fun getDataList():List<List<OKGameBean>>{
        return dataList
    }

    fun notifyDataChange(){
        mAdapter.notifyDataSetChanged()
    }

    //分组数据
    private fun groupDataList(cutData: List<OKGameBean>) {
        //按pageSize=6  获得总页码
        var totalPage: Int = cutData.size / pageSize
        if (cutData.size % pageSize != 0) {
            totalPage += 1
        }
        mAdapter.isSinglePage=totalPage==1
        //按页分组数据
        for (i in 0 until totalPage) {
            //该页开始位置
            val startPosition = i * pageSize
            //该页结束
            val endPosition = startPosition + pageSize
            if (endPosition > cutData.size) {
                dataList.add(cutData.subList(startPosition, cutData.size))
            } else {
                dataList.add(cutData.subList(startPosition, endPosition))
            }
        }
        mAdapter.setList(dataList)
    }

    //设置Icon
    fun setIcon(resource: Int): GamesPageView {
        binding.ivIcon.setImageResource(resource)
        return this
    }
    fun setIcon(resource: String?): GamesPageView {
        binding.ivIcon.load(resource)
        return this
    }

    //模块名称
    fun setCategoryName(name: Int): GamesPageView {
        binding.tvName.setText(name)
        return this
    }
    fun setCategoryName(name: String?): GamesPageView {
        binding.tvName.text = name
        return this
    }

    //更多点击
    fun setOnMoreClick(block: () -> Unit): GamesPageView {
        onMoreClick = block
        return this
    }

    //游戏点击
    fun setOnGameClick(block: (data: OKGameBean) -> Unit): GamesPageView {
        onGameClick = block
        return this
    }

    //点击收藏
    fun setOnFavoriteClick(block: (item: OKGameBean) -> Unit): GamesPageView {
        onFavoriteClick = block
        return this
    }


    //更多，换页按钮是否显示
    private fun setViewVisible(flag: Boolean) {
        binding.run {
            tvMore.isVisible = flag
            ivBackPage.isVisible = flag
            ivForwardPage.isVisible = flag
        }
    }

    private fun scrollRecycler(isNext:Boolean){
        binding.rvGameItem.let {
            val manager = it.layoutManager as LinearLayoutManager
            var position=manager.findFirstVisibleItemPosition()
            if(isNext){
                position += 1
            }else{
                position -= 1
            }
            if (position > manager.itemCount - 1) {
                return
            }
            if (position < 0) {
                position = 0
            }
            it.smoothScrollToPosition(position)
        }
    }

    private fun setRecyclerScrollListener() {
        binding.rvGameItem.let {
            val manager = it.layoutManager as LinearLayoutManager
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (RecyclerView.SCROLL_STATE_IDLE == newState) {

                        when (manager.findFirstCompletelyVisibleItemPosition()) {
                            0 -> {
                                binding.ivBackPage.alpha = 0.5f
                                binding.ivBackPage.isEnabled = false
                                binding.ivForwardPage.alpha = 1f
                                binding.ivForwardPage.isEnabled = true
                            }

                            dataList.size - 1 -> {
                                binding.ivBackPage.alpha = 1f
                                binding.ivBackPage.isEnabled = true
                                binding.ivForwardPage.alpha = 0.5f
                                binding.ivForwardPage.isEnabled = false
                            }

                            else -> {
                                binding.ivBackPage.alpha = 1f
                                binding.ivBackPage.isEnabled = true
                                binding.ivForwardPage.alpha = 1f
                                binding.ivForwardPage.isEnabled = true
                            }
                        }     
                    }
                }
            })
        }
    }
}