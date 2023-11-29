package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.network.infoCenter.InfoCenterRequest
import org.cxct.sportlottery.network.infoCenter.InfoCenterResult
import retrofit2.Response
import timber.log.Timber

enum class MsgType(var code: Int) {
    NOTICE_UNREAD(0),
    NOTICE_READED(1)
}

class InfoCenterRepository {

    val unreadNoticeList: LiveData<List<InfoCenterData>>
        get() = _unreadNoticeList
    private val _unreadNoticeList = MutableLiveData<List<InfoCenterData>>().apply {
        value = listOf()
    }

    val unreadList: LiveData<List<InfoCenterData>>
        get() = _unreadList
    private val _unreadList = MutableLiveData<List<InfoCenterData>>().apply {
        value = listOf()
    }
    var moreUnreadList = mutableListOf<InfoCenterData>()

    val readedList: LiveData<List<InfoCenterData>>
        get() = _readedList
    private val _readedList = MutableLiveData<List<InfoCenterData>>().apply {
        value = listOf()
    }

    //未讀總數目
    val totalUnreadMsgCount: LiveData<Int>
        get() = _totalUnreadMsgCount
    private var _totalUnreadMsgCount = MutableLiveData<Int>()

    //已讀總數目
    val totalReadMsgCount: LiveData<Int>
        get() = _totalReadMsgCount
    private var _totalReadMsgCount = MutableLiveData<Int>()

    suspend fun getUserNoticeList(infoCenterRequest: InfoCenterRequest): Response<InfoCenterResult> {
        val response = OneBoSportApi.infoCenterService.getInfoList(infoCenterRequest)

        if (response.isSuccessful) {
            when (infoCenterRequest.isRead) {
                MsgType.NOTICE_UNREAD.code -> {
                    response.body().let {
                        moreUnreadList = mutableListOf()
                        val newUnreadNoticeList = it?.infoCenterData?.filter { infoCenterData ->
                            infoCenterData.isRead.toString() == MsgType.NOTICE_UNREAD.code.toString()
                        } as MutableList<InfoCenterData>

                        if(infoCenterRequest.page !=1 ){
                            if (!_unreadList.value.isNullOrEmpty())
                                moreUnreadList = _unreadList.value as MutableList<InfoCenterData>
                        }

                        newUnreadNoticeList.forEach { infoCenterData ->
                            moreUnreadList.add(infoCenterData)
                        }

                        _unreadList.value = moreUnreadList
                        _unreadNoticeList.value = moreUnreadList

                        _totalUnreadMsgCount.postValue(it.total ?: 0)
                    }
                }
                MsgType.NOTICE_READED.code -> {
                    response.body().let {
                        _readedList.postValue(it?.infoCenterData?.filter { infoCenterData ->
                            infoCenterData.isRead.toString() == MsgType.NOTICE_READED.code.toString()
                        })
                        _totalReadMsgCount.postValue(it?.total?:0)
                    }
                }
            }
        }
        return response
    }

    suspend fun setMsgRead(msgId: String): Response<InfoCenterResult> {
        val response = OneBoSportApi.infoCenterService.setMsgReaded(msgId)

        if (response.isSuccessful) {
            val noticeList = _unreadList.value?.toMutableList()
            noticeList?.find { it.id.toString() == msgId }?.let { noticeList?.remove(it) }
            _unreadNoticeList.postValue(noticeList?.toList() ?: listOf())
//            response.body()?.total?.let { _totalUnreadMsgCount.postValue(it) }
        }

        return response
    }

    //只取資料筆數
    suspend fun getMsgCount(dataType: Int) {
        val infoCenterRequest = InfoCenterRequest(1, 1, dataType)
        val response = OneBoSportApi.infoCenterService.getInfoList(infoCenterRequest)

        if (response.isSuccessful) {
            when (dataType) {
                MsgType.NOTICE_UNREAD.code -> {
                    response.body().let {
                        _totalUnreadMsgCount.postValue(it?.total ?: 0)
                    }
                }
                MsgType.NOTICE_READED.code -> {
                    response.body().let {
                        _totalReadMsgCount.postValue(it?.total ?: 0)
                    }
                }
            }
        }
    }

    fun setUserNoticeList(userNoticeList: List<FrontWsEvent.UserNotice>) {

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
            it.isRead.toString() == MsgType.NOTICE_UNREAD.code.toString()
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

    fun clearList() {
        _unreadList.postValue(listOf())
        _readedList.postValue(listOf())
    }
}