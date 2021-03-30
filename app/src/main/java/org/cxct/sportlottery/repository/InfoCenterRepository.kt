package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.infoCenter.InfoCenterRequest
import org.cxct.sportlottery.network.infoCenter.InfoCenterResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.network.service.user_notice.UserNotice
import retrofit2.Response


const val NOTICE_UNREAD = 0

class InfoCenterRepository {

    val unreadNoticeList: LiveData<List<InfoCenterData>>
        get() = _unreadNoticeList

    private val _unreadNoticeList = MutableLiveData<List<InfoCenterData>>().apply {
        value = listOf()
    }

    suspend fun getUserNoticeList(infoCenterRequest: InfoCenterRequest): Response<InfoCenterResult> {
        val response = OneBoSportApi.infoCenterService.getInfoList(infoCenterRequest)

        if (response.isSuccessful) {
            response.body().let {
                _unreadNoticeList.postValue(it?.infoCenterData?.filter { infoCenterData ->
                    infoCenterData.isRead == NOTICE_UNREAD
                })
            }
        }

        return response
    }

    suspend fun setMsgRead(msgId: String): Response<InfoCenterResult> {
        val response = OneBoSportApi.infoCenterService.setMsgReaded(msgId)

        if (response.isSuccessful) {
            val noticeList = _unreadNoticeList.value?.toMutableList()
            val noticeRead = noticeList?.find {
                it.id == msgId.toInt()
            }
            noticeList?.remove(noticeRead)
            _unreadNoticeList.postValue(noticeList?.toList() ?: listOf())
        }

        return response
    }

    fun setUserNoticeList(userNoticeList: List<UserNotice>) {
        val noticeList = _unreadNoticeList.value?.toMutableList()
        val unreadUserNoticeList = userNoticeList.map {
            InfoCenterData(
                it.id,
                it.userId,
                it.userName,
                it.addDate.toString(),
                it.title,
                it.content,
                it.isRead,
                it.noticeType,
                it.msgShowType,
                it.platformId,
                it.operatorName
            )
        }.filter {
            it.isRead == NOTICE_UNREAD
        }
        unreadUserNoticeList.forEach {
            noticeList?.let { noticeList ->
                if (!noticeList.contains(it)) {
                    noticeList.add(it)
                }
            }
        }

        _unreadNoticeList.postValue(noticeList?.toList() ?: listOf())
    }

    fun clear() {
        _unreadNoticeList.postValue(listOf())
    }
}