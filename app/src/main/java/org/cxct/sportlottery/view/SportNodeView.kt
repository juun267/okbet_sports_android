package org.cxct.sportlottery.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
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
                    recyclerNode.unExpand(mNodeList.size*40.dp)
                    false
                }else{
                    //展开
                    ivWay.setImageResource(R.drawable.ic_node_open)
                    recyclerNode.expand(mNodeList.size*40.dp)
                    true
                }

            }
        }

    }


    /**
     * 设置选项数据
     */
    fun setNodeChild(nodes: ArrayList<NodeBean>):SportNodeView{
        //item子项
        mNodeList=nodes
        childAdapter.data= mNodeList
        return this
    }


    /**
     * 设置title
     */
    fun setTitle(title:String):SportNodeView{
        //标题
        binding.tvTitle.text=title
        return  this
    }


    /**
     * 选中点击
     */
    @SuppressLint("NotifyDataSetChanged")
    fun  setOnChildClick(block:(data:NodeBean)->Unit):SportNodeView{
        childAdapter.setOnItemClickListener{_,_,position->
            val item=childAdapter.data[position]
            clearSelect()
            item.select=true
            childAdapter.notifyDataSetChanged()
            block(item)
        }
        return this
    }


    private fun clearSelect(){
        childAdapter.data.forEach {
            it.select=false
        }
    }

}