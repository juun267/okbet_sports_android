package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.databinding.ViewSportNodeBinding
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerNodeAdapter

class SportNodeView(context: Context, attrs: AttributeSet) :LinearLayout(context,attrs){
    private val childAdapter= RecyclerNodeAdapter()
    val binding: ViewSportNodeBinding

    init {
        orientation= VERTICAL
        binding=ViewSportNodeBinding.inflate(LayoutInflater.from(context),this,true)
        initView()
    }


    private fun initView(){
        binding.recyclerNode.layoutManager=LinearLayoutManager(context)
        binding.recyclerNode.adapter=childAdapter

        childAdapter.data= arrayListOf("","","","")
    }
}