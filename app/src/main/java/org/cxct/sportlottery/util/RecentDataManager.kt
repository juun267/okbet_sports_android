package org.cxct.sportlottery.util

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.net.games.data.OKGameBean

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
        //如果记录跟最新一个相同，则不重复记录
//        val first = recentList.firstOrNull()
//        if (first!=null&&first.recordType==record.recordType&&first.gameBean?.firmType == record.gameBean?.firmType)
//            return
        recentList.add(0,record)
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
data class RecentRecord(val recordType: Int,val gameType: String?=null,val gameBean: OKGameBean?=null)

