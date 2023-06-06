package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewSportNodeBinding
import org.cxct.sportlottery.ui.maintab.entity.NodeBean
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerNodeAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp

class SportNodeView(context: Context, attrs: AttributeSet) :LinearLayout(context,attrs){
    //item adapter
    private val childAdapter= RecyclerNodeAdapter()
    //item list
    private var mNodeList= arrayListOf<NodeBean>()

    //是否展开
    private var isExpand=false

    val binding: ViewSportNodeBinding

    init {
        orientation= VERTICAL
        binding=ViewSportNodeBinding.inflate(LayoutInflater.from(context),this)
        initView()
    }


    private fun initView(){
        binding.apply {
            recyclerNode.layoutManager=LinearLayoutManager(context)
            recyclerNode.adapter=childAdapter

            //点击标题 展开/收起
            frameTitle.onClick {
                isExpand = if(isExpand){
                    //收起
                    ivWay.setImageResource(R.drawable.ic_node_close)
                    recyclerNode.unExpand(mNodeList.size*32.dp)
                    false
                }else{
                    //展开
                    ivWay.setImageResource(R.drawable.ic_node_open)
                    recyclerNode.expand(mNodeList.size*32.dp)
                    true
                }

            }
        }

    }


    /**
     * 设置选项数据
     */
    fun setNodeData(title:String,nodes: ArrayList<NodeBean>){
        //标题
        binding.tvTitle.text=title
        //item子项
        mNodeList=nodes
        childAdapter.data= mNodeList
    }
}