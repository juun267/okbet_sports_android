package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ViewJacketPotBinding
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerJackPotAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil
import org.cxct.sportlottery.util.TextUtil
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
    //背景图后面的间距不同
    private var rollerLineWidth2=0.0
    //滚筒每个格子宽度   占5.3%
    private var rollerItemWidth=0.0

    //第一个
    private var adapter1:RecyclerJackPotAdapter?=null
    private var mLayoutManager1:AdjustLinearLayoutManager? = null

    //第二个
    private var adapter2:RecyclerJackPotAdapter?=null
    private var mLayoutManager2:AdjustLinearLayoutManager? = null

    private var adapter3:RecyclerJackPotAdapter?=null
    private var mLayoutManager3:AdjustLinearLayoutManager? = null

    private var adapter4:RecyclerJackPotAdapter?=null
    private var mLayoutManager4:AdjustLinearLayoutManager? = null

    private var adapter5:RecyclerJackPotAdapter?=null
    private var mLayoutManager5:AdjustLinearLayoutManager? = null

    private var adapter6:RecyclerJackPotAdapter?=null
    private var mLayoutManager6:AdjustLinearLayoutManager? = null

    private var adapter7:RecyclerJackPotAdapter?=null
    private var mLayoutManager7:AdjustLinearLayoutManager? = null

    private var adapter8:RecyclerJackPotAdapter?=null
    private var mLayoutManager8:AdjustLinearLayoutManager? = null

    private var adapter9:RecyclerJackPotAdapter?=null
    private var mLayoutManager9:AdjustLinearLayoutManager? = null

    private var adapter10:RecyclerJackPotAdapter?=null
    private var mLayoutManager10:AdjustLinearLayoutManager? = null

    private var adapter11:RecyclerJackPotAdapter?=null
    private var mLayoutManager11:AdjustLinearLayoutManager? = null

    private var adapter12:RecyclerJackPotAdapter?=null
    private var mLayoutManager12:AdjustLinearLayoutManager? = null

    private var adapter13:RecyclerJackPotAdapter?=null
    private var mLayoutManager13:AdjustLinearLayoutManager? = null

    private var adapter14:RecyclerJackPotAdapter?=null
    private var mLayoutManager14:AdjustLinearLayoutManager? = null

    private val textList= arrayListOf<TextView>()
    private val recyclerList= arrayListOf<RecyclerView>()
    private val adapterList= arrayListOf(adapter1,adapter2,adapter3,adapter4,adapter5,adapter6,adapter7,adapter8,adapter9,adapter10,adapter11,adapter12,adapter13,adapter14)
    private val managerList= arrayListOf(mLayoutManager1,mLayoutManager2,mLayoutManager3,mLayoutManager4,mLayoutManager5,mLayoutManager6
            ,mLayoutManager7,mLayoutManager8,mLayoutManager9,mLayoutManager10,mLayoutManager11,mLayoutManager12,mLayoutManager13,mLayoutManager14)
    init {
        binding=ViewJacketPotBinding.inflate(LayoutInflater.from(context), this,true)

        //滚筒图宽度  -边距
        rollerWidth=ScreenUtil.getScreenWidth(context)-13.dp-12.dp
        rollerHeadWidth=rollerWidth*0.046
        rollerLineWidth=rollerWidth*0.009
        rollerLineWidth2=rollerWidth*0.008
        rollerItemWidth=rollerWidth*0.053
        initViewList()
        setJackPotNumber(5326.0)
    }

    fun setJackPotNumber(number:Double){
        //格式化金额  000,000,000.00
        val numberStr= TextUtil.format2(number)
        numberStr?.let {
            //拆分成数组
            val numberArray=numberStr.toCharArray()
            //循环初始化每个数字，开始滚动
            for (i in numberArray.indices){
                when(i){
                    0->{
                        //第一个数字  间距不一样
                        initRoller(i,textList[i],recyclerList[i],adapterList[i],managerList[i],numberArray[i].digitToInt(),true)
                    }
                    3,7->{
                        //逗号
                        initRoller(i,textList[i],recyclerList[i],adapterList[i],managerList[i])
                    }
                    11->{
                        //小数点
                        initRoller(i,textList[i],recyclerList[i],adapterList[i],managerList[i],true)
                    }
                    else->{
                        //普通数字
                        initRoller(i,textList[i],recyclerList[i],adapterList[i],managerList[i],numberArray[i].digitToInt())
                    }
                }
            }
        }

    }

    private fun initRoller(position:Int,textview: TextView, recycler:RecyclerView,adapter:RecyclerJackPotAdapter?, manager:AdjustLinearLayoutManager?,number:Int,isHead:Boolean=false){
        //默认第一轮滚动 0-9
        val rollerList=arrayListOf("0","1","2","3","4","5","6","7","8","9")
        if(number==0){
            rollerList.add("0")
        }
        //循环添加第二轮  0-X
        for(i in 0 .. number){
            rollerList.add(i.toString())
        }

        //初始化间距，recycler数据
        textview.postDelayed({
            //头尾间距不一样
            if(isHead){
                //第一个view间隔
                textview.setPadding(rollerHeadWidth.toInt(),0,0,0)
            }else{
                //普通间隔
                if(position>=5){
                    textview.setPadding(rollerLineWidth2.toInt(),0,0,0)
                }else{
                    textview.setPadding(rollerLineWidth.toInt(),0,0,0)
                }
            }
            adapter?.setList(rollerList)
            recycler.layoutManager=manager
            recycler.adapter=adapter
        },100)

        //随机启动时间
        val randomInt = (1000..3000).random()
        textview.postDelayed({
            manager?.setMillisecondsPerInch(adapter!!.dataCount()*100f)
            //开启recycler滚动
            recycler.smoothScrollToPosition(adapter!!.dataCount()-1)
        },randomInt.toLong())
    }


    private fun initRoller(position:Int,textview: TextView, recycler:RecyclerView,adapter:RecyclerJackPotAdapter?, manager:AdjustLinearLayoutManager?,isPoint:Boolean=false){
        textview.postDelayed({
            if(position>=5){
                textview.setPadding(rollerLineWidth2.toInt(),0,0,0)
            }else{
                textview.setPadding(rollerLineWidth.toInt(),0,0,0)
            }
            if(isPoint){
                adapter?.setList(arrayListOf("."))
            }else{
                adapter?.setList(arrayListOf(","))
            }
            recycler.layoutManager=manager
            recycler.adapter=adapter
        },100)
    }


    private fun initViewList(){
        textList.add(binding.tv1)
        textList.add(binding.tv2)
        textList.add(binding.tv3)
        textList.add(binding.tv4)
        textList.add(binding.tv5)
        textList.add(binding.tv6)
        textList.add(binding.tv7)
        textList.add(binding.tv8)
        textList.add(binding.tv9)
        textList.add(binding.tv10)
        textList.add(binding.tv11)
        textList.add(binding.tv12)
        textList.add(binding.tv13)
        textList.add(binding.tv14)

        recyclerList.add(binding.recycler1)
        recyclerList.add(binding.recycler2)
        recyclerList.add(binding.recycler3)
        recyclerList.add(binding.recycler4)
        recyclerList.add(binding.recycler5)
        recyclerList.add(binding.recycler6)
        recyclerList.add(binding.recycler7)
        recyclerList.add(binding.recycler8)
        recyclerList.add(binding.recycler9)
        recyclerList.add(binding.recycler10)
        recyclerList.add(binding.recycler11)
        recyclerList.add(binding.recycler12)
        recyclerList.add(binding.recycler13)
        recyclerList.add(binding.recycler14)

        //禁止触碰滚动
        recyclerList.forEach {
            it.setOnTouchListener { v, event ->
                 true
            }
        }
        for(i in 0 until adapterList.size){
            adapterList[i]=RecyclerJackPotAdapter(rollerItemWidth.toInt())
        }

        for(i in 0 until  managerList.size){
            managerList[i]=AdjustLinearLayoutManager(context)
            managerList[i]?.setScrollType(LinearSmoothScroller.SNAP_TO_START)
        }
    }
}