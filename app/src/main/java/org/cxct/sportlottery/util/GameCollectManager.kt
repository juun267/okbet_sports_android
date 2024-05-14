package org.cxct.sportlottery.util

import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.common.extentions.toIntS
import java.math.RoundingMode

object GameCollectManager {
    val gameCollectNum = MutableLiveData<MutableMap<String,String>>()

    fun TextView.showCollectAmount(gameEntryId: Int){
        val amount = gameCollectNum.value?.getOrDefault(gameEntryId.toString(),"0").toIntS(0)
        text = when{
            amount  < 1000 -> "$amount"
            amount  < 1000000 -> ArithUtil.round(ArithUtil.div(amount.toDouble(),1000.0),1,
                RoundingMode.HALF_UP)+"K"
            else -> ArithUtil.round(ArithUtil.div(amount.toDouble(),1000000.0),1, RoundingMode.HALF_UP)+"M"
        }
    }

    fun addCollectNum(gameEntryId: Int,markCollect:Boolean){
         gameCollectNum.value?.let {
             val key = gameEntryId.toString()
             var originNum = it[key].toIntS(0)
             it[key] = (if(markCollect){
                  1
             }else {
                 -1
             }+originNum).toString()
        }
    }
}