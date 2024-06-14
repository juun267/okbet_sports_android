package org.cxct.sportlottery.util

import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.common.GameType

object RecentDataManager {
   private const val RECENT_RECORD = "recentRecord"
   private val recentList = mutableListOf<RecentRecord>()
   val recentEvent = SingleLiveEvent<MutableList<RecentRecord>>()

  init {
      KvUtils.decodeString(RECENT_RECORD).let {
          if (!it.isNullOrEmpty()){
              it.fromJson<List<RecentRecord>>()?.let {
                      it1 -> recentList.addAll(it1)
              }
          }
      }
  }
    fun addRecent(record: RecentRecord){
        //如果记录有同类型的数据，则移除，并且把新数据加入第一个
        recentList.removeAll { it == record }
        recentList.add(0,record)
//        LogUtil.toJson(record)
        KvUtils.put(RECENT_RECORD,recentList.toJson())
        recentEvent.postValue(recentList)
    }
    fun getRecentList():MutableList<RecentRecord>{
        return recentList
    }
}

/**
 * recordType 0体育 1游戏
 */
@KeepMembers
data class RecentRecord(val recordType: Int,val gameType: String?=null,val categoryCode: String?=null,val gameBean: OKGameBean?=null){
    override fun equals(other: Any?): Boolean {
        if (other is RecentRecord){
            return recordType==other.recordType&&gameType==other.gameType&&categoryCode==other.categoryCode&&gameBean?.firmType==other.gameBean?.firmType
        }
        return false
    }

}

