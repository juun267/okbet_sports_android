package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearSmoothScroller
import org.cxct.sportlottery.databinding.ViewJacketPotBinding
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerJackPotAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil
import org.cxct.sportlottery.view.widget.AdjustLinearLayoutManager

class JackpotView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {
    private val binding:ViewJacketPotBinding
    //滚筒整体宽度
    private var rollerWidth=0
    //滚筒头尾宽度 占4.6%
    private var rollerHeadWidth=0.0
    //滚筒每个格子之间距离  占0.8%
    private var rollerLineWidth=0.0
    //滚筒每个格子宽度   占5.3%
    private var rollerItemWidth=0.0

    private var adapter1:RecyclerJackPotAdapter?=null
    private var mLayoutManager1:AdjustLinearLayoutManager? = null

    init {
        binding=ViewJacketPotBinding.inflate(LayoutInflater.from(context), this,true)

        //滚筒图宽度  -边距
        rollerWidth=ScreenUtil.getScreenWidth(context)-13.dp-12.dp
        rollerHeadWidth=rollerWidth*0.046
        rollerLineWidth=rollerWidth*0.008
        rollerItemWidth=rollerWidth*0.053
        adapter1=RecyclerJackPotAdapter(rollerItemWidth.toInt())
        mLayoutManager1=AdjustLinearLayoutManager(context)
        mLayoutManager1?.setScrollType(LinearSmoothScroller.SNAP_TO_START)
        initView()
    }

    private fun initView(){
        binding.run {
            tv1.postDelayed({
                tv1.setPadding(rollerHeadWidth.toInt(),0,0,0)
                adapter1?.setList(arrayListOf("0","1","2","3","4","5","6","7","8","9","0","1","2","3"))
                recycler1.layoutManager=mLayoutManager1
                recycler1.adapter=adapter1
            },100)

//            tv1.postDelayed({
//                mLayoutManager1?.setMillisecondsPerInch(adapter1!!.dataCount()*100f)
//                recycler1.smoothScrollToPosition(adapter1!!.dataCount()-1)
//            },3000)
        }
    }
}