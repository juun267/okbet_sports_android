package org.cxct.sportlottery.util

import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.net.games.data.OKGameBean

object GameCollectManager {
    //三方游戏的收藏数量记录
    val gameCollectNum = MutableLiveData<MutableMap<String,String>>()
    //当前用户的收藏记录
    val collectGameList = MutableLiveData<MutableList<OKGameBean>>()
    val collectLiveList = MutableLiveData<MutableList<OKGameBean>>()
    private val collectStatus = MutableSharedFlow<Pair<Int,Boolean>>()

    fun observerGameCollect(lifecycleOwner: LifecycleOwner, block: (Pair<Int,Boolean>)-> Unit) {
        collectStatus.collectWith(lifecycleOwner.lifecycleScope, block)
    }

    fun TextView.showCollectAmount(gameEntryId: Int){
        val amount = gameCollectNum.value?.getOrDefault(gameEntryId.toString(),"0").toIntS(0)
        text = when{
            amount  < 1000 -> "$amount"
            amount  < 1000000 -> ArithUtil.div(amount.toDouble(),1000.0,1).toString()+"K"
            else -> ArithUtil.div(amount.toDouble(),1000000.0,1).toString() +"M"
        }
    }

    fun addCollectNum(gameEntryId: Int,markCollect:Boolean){
         gameCollectNum.value?.let {
             val key = gameEntryId.toString()
             var originNum = it[key].toIntS(0)
             it[key] = (if(markCollect){
                  1
             }else {
                  0
             }+originNum).toString()
        }
    }

    fun setUpGameCollect(okgameBeans: MutableList<OKGameBean>){
        collectGameList.postValue(okgameBeans)
    }
    fun setUpLiveCollect(okgameBeans: MutableList<OKGameBean>){
        collectLiveList.postValue(okgameBeans)
    }

    fun updateCollect(okgameBean: OKGameBean, gameEntryType: String){
        val event = if(gameEntryType==GameEntryType.OKGAMES) collectGameList else collectLiveList
        val list = event.value?: mutableListOf()
        if (okgameBean.markCollect){
            if (!list.any { it.id==okgameBean.id }) {
                list.add(okgameBean)
            }
        }else{
            list.removeIf { okgameBean.id == it.id }
        }
        event.postValue(list)
        MainScope().launch {
            collectStatus.emit(Pair(okgameBean.id, okgameBean.markCollect))
        }
    }
}