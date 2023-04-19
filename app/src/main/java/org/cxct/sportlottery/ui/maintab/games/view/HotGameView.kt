package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_hot_game.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerHotGameAdapter
import org.cxct.sportlottery.view.DividerItemDecorator

class HotGameView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private  var gameList:List<String>?=null
    private val adapter= RecyclerHotGameAdapter()
    init {
        initView()
    }

    private fun initView(){
        LayoutInflater.from(context).inflate(R.layout.view_hot_game, this, true)

        recycler_hot_game.let {
            val manager=LinearLayoutManager(context)
            manager.orientation=LinearLayoutManager.HORIZONTAL
            it.layoutManager=manager
            it.adapter=adapter
            it.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_trans)))
        }

        setGameData()
    }

    //data:List<Recommend>
    fun setGameData(){
        val temp=ArrayList<String>()
        temp.add("")
        temp.add("")
        temp.add("")
        temp.add("")
        temp.add("")

        gameList=temp
        adapter.setList(gameList)
    }

}