package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewHomeFollowBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerHomeFollowAdapter
import org.cxct.sportlottery.ui.maintab.games.bean.FollowMenuBean
import org.cxct.sportlottery.util.JumpUtil.toExternalWeb
import org.cxct.sportlottery.util.SpaceItemDecoration
import splitties.systemservices.layoutInflater
import splitties.views.recyclerview.horizontalLayoutManager

class HomeFollowView(
    context: Context, attrs: AttributeSet
) : LinearLayout(context, attrs) {
    private val menuList = arrayListOf<FollowMenuBean>()
    private val mAdapter = RecyclerHomeFollowAdapter()

    val binding = ViewHomeFollowBinding.inflate(layoutInflater,this,true)

    init {
        initView()
    }

    private fun initView() = binding.run{
        rvFollow.apply {
            layoutManager = LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
            addItemDecoration(SpaceItemDecoration(context,R.dimen.margin_8))
            adapter = mAdapter
        }
        initHomeFollowData()
    }


    /**
     * 底部是否显示社交view   只有首页需要
     */
//    fun showFollowView(){
//        initHomeFollowData()
//    }


    /**
     * 初始化社交view
     */
    private fun initHomeFollowData() {
        //未获取到数据 隐藏
        if (sConfigData == null) {
            gone()
            return
        }
        visible()


        //初始化各个社交按钮
        sConfigData?.let {
            val faceBookMenu = FollowMenuBean(
                context.getString(R.string.facebookFollow),
                R.drawable.ic_home_gray_facebook,
                it.facebookLinkConfig
            )
            val instagramMenu = FollowMenuBean(
                context.getString(R.string.instagramFollow),
                R.drawable.ic_home_gray_ins,
                it.instagramLinkConfig
            )
            val youTubeMenu = FollowMenuBean(
                context.getString(R.string.youtubeFollow),
                R.drawable.ic_home_gray_youtube,
                it.youtubeLinkConfig
            )
            val twitterMenu = FollowMenuBean(
                context.getString(R.string.twitterFollow),
                R.drawable.ic_home_gray_twiter,
                it.twitterLinkConfig
            )
            val tiktokMenu = FollowMenuBean(
                context.getString(R.string.tiktokFollow),
                R.drawable.ic_home_gray_tik,
                it.tiktokLinkConfig
            )
            val whatAppMenu = FollowMenuBean(
                context.getString(R.string.whats_app),
                R.drawable.ic_home_gray_whatsapp,
                it.whatsappLinkConfig
            )
            val telegramMenu = FollowMenuBean(
                context.getString(R.string.telegram),
                R.drawable.ic_home_gray_telegram,
                it.telegramLinkConfig
            )
            val vlberMenu = FollowMenuBean(
                context.getString(R.string.viber),
                R.drawable.ic_home_gray_viber,
                it.viberLinkConfig
            )

            //添加社交按钮
            initListData(
                youTubeMenu,
                faceBookMenu,
                instagramMenu,
                twitterMenu,
                tiktokMenu,
                whatAppMenu,
                telegramMenu,
                vlberMenu
            )

            //没数据隐藏
            if (menuList.isEmpty()) {
                gone()
            }
            mAdapter.data = menuList
            binding.rvFollow.adapter = mAdapter

            //item点击跳转浏览器
            mAdapter.setOnItemClickListener { _, _, position ->
                val item = mAdapter.data[position]
                toExternalWeb(context, item.url)
            }
        }

    }


    /**
     * 初始化社交item数据
     */
    private fun initListData(vararg item: FollowMenuBean) {
        //遍历所有社交按钮
        item.forEach {
            //url不为空， 才添加显示
            if (!it.url.isNullOrEmpty()) {
                menuList.add(it)
            }
        }
    }
}