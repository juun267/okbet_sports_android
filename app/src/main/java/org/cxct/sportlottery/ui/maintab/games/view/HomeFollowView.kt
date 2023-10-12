package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerHomeFollowAdapter
import org.cxct.sportlottery.ui.maintab.games.bean.FollowMenuBean
import org.cxct.sportlottery.util.JumpUtil.toExternalWeb

class HomeFollowView(
    context: Context, attrs: AttributeSet
) : LinearLayout(context, attrs) {
    private val menuList = arrayListOf<FollowMenuBean>()
    private val mAdapter = RecyclerHomeFollowAdapter()
    private var mRecyclerMenu: RecyclerView? = null

    init {
        orientation = VERTICAL
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_home_follow, this, true)
        mRecyclerMenu = findViewById(R.id.recyclerMenu)
        val manager = GridLayoutManager(context, 4)
        mRecyclerMenu?.layoutManager = manager
        mRecyclerMenu?.adapter = mAdapter
        initHomeFollowData()
    }

    fun setHalloweenStyle() {
        findViewById<ImageView>(R.id.ivFollowUs).setImageResource(R.drawable.ic_home_follow_us_h)

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
                R.drawable.ic_home_facebook,
                it.facebookLinkConfig
            )
            val instagramMenu = FollowMenuBean(
                context.getString(R.string.instagramFollow),
                R.drawable.ic_home_ins,
                it.instagramLinkConfig
            )
            val youTubeMenu = FollowMenuBean(
                context.getString(R.string.youtubeFollow),
                R.drawable.ic_home_youtube,
                it.youtubeLinkConfig
            )
            val twitterMenu = FollowMenuBean(
                context.getString(R.string.twitterFollow),
                R.drawable.ic_home_twiter,
                it.twitterLinkConfig
            )
            val tiktokMenu = FollowMenuBean(
                context.getString(R.string.tiktokFollow),
                R.drawable.ic_home_tik,
                it.tiktokLinkConfig
            )
            val whatAppMenu = FollowMenuBean(
                context.getString(R.string.whats_app),
                R.drawable.ic_home_whatsapp,
                it.whatsappLinkConfig
            )
            val telegramMenu = FollowMenuBean(
                context.getString(R.string.telegram),
                R.drawable.ic_home_telegram,
                it.telegramLinkConfig
            )
            val vlberMenu = FollowMenuBean(
                context.getString(R.string.viber),
                R.drawable.ic_home_viber,
                it.viberLinkConfig
            )

            //添加社交按钮
            initListData(
                faceBookMenu,
                instagramMenu,
                youTubeMenu,
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
            mRecyclerMenu?.adapter = mAdapter

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