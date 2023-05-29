package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerHomeFollowAdapter
import org.cxct.sportlottery.ui.maintab.games.bean.FollowMenuBean

class HomeFollowView(context: Context, attrs: AttributeSet
) : LinearLayout(context, attrs)  {
    private val menuList= arrayListOf<FollowMenuBean>()
    private val mAdapter= RecyclerHomeFollowAdapter()
    private var mRecyclerMenu:RecyclerView?=null
    init {
        orientation=VERTICAL
        initView()
    }

    private fun initView(){
        LayoutInflater.from(context).inflate(R.layout.view_home_follow, this, true)
        mRecyclerMenu=findViewById(R.id.recyclerMenu)
        val manager=GridLayoutManager(context,5)
        mRecyclerMenu?.layoutManager=manager
        mRecyclerMenu?.adapter=mAdapter
        initHomeFollowData()
    }


    fun initHomeFollowData(){
        val faceBookMenu= FollowMenuBean("Facebook",R.drawable.ic_home_facebook,true)
        val instagramMenu= FollowMenuBean("Instagram",R.drawable.ic_home_ins,true)
        val youTubeMenu= FollowMenuBean("YouTube",R.drawable.ic_home_youtube,true)
        val twitterMenu= FollowMenuBean("Twitter",R.drawable.ic_home_twiter,true)
        val tiktokMenu= FollowMenuBean("Tiktok",R.drawable.ic_home_tik,true)
        menuList.add(faceBookMenu)
        menuList.add(instagramMenu)
        menuList.add(youTubeMenu)
        menuList.add(twitterMenu)
        menuList.add(tiktokMenu)

        mAdapter.data=menuList
        mRecyclerMenu?.adapter=mAdapter
    }
}