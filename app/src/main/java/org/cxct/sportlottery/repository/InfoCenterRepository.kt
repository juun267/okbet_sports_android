package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.network.infoCenter.InfoCenterRequest
import org.cxct.sportlottery.network.infoCenter.InfoCenterResult
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import retrofit2.Response

enum class MsgType(var code: Int) {
    NOTICE_UNREAD(0),
    NOTICE_READED(1)
}

object InfoCenterRepository {

    val unreadNoticeList: LiveData<List<InfoCenterData>>
        get() = _unreadNoticeList
    private val _unreadNoticeList = MutableLiveData<List<InfoCenterData>>(listOf())

    val unreadList: LiveData<List<InfoCenterData>>
        get() = _unreadNoticeList

    var moreUnreadList = mutableListOf<InfoCenterData>()

    val readedList: LiveData<List<InfoCenterData>>
        get() = _readedList
    private val _readedList = MutableLiveData<List<InfoCenterData>>(listOf())

    //未讀總數目
    val totalUnreadMsgCount: LiveData<Int>
        get() = _totalUnreadMsgCount
    private var _totalUnreadMsgCount = MutableLiveData<Int>()

    //已讀總數目
    val totalReadMsgCount: LiveData<Int>
        get() = _totalReadMsgCount
    private var _totalReadMsgCount = MutableLiveData<Int>()

    init {
        ServiceBroadcastReceiver.userNotice.observeForever  {
            it.getContentIfNotHandled()?.userNoticeListList?.let { list ->
                setUserNoticeList(list)
            }
        }
    }

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


                        if (newUnreadNoticeList.isNotEmpty()) {
                            moreUnreadList.addAll(newUnreadNoticeList)
                        }

                        _unreadNoticeList.value = moreUnreadList
                        it.page = infoCenterRequest.page
                        _totalUnreadMsgCount.postValue(it.total ?: 0)
                    }
                }
                MsgType.NOTICE_READED.code -> {
                    response.body().let {
                        _readedList.postValue(it?.infoCenterData?.filter { infoCenterData ->
                            infoCenterData.isRead.toString() == MsgType.NOTICE_READED.code.toString()
                        })
                        it?.page = infoCenterRequest.page
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
            val noticeList = _unreadNoticeList.value?.toMutableList()
            noticeList?.find { it.id.toString() == msgId }?.let { noticeList.remove(it) }
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
        val unreadUserNoticeList = mutableListOf<InfoCenterData>()
        userNoticeList.forEach {
            if (it.isRead == MsgType.NOTICE_UNREAD.code.toLong()) {
                unreadUserNoticeList.add(InfoCenterData(
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
                ))
            }
        }

        if (noticeList.isNullOrEmpty()) {
            _unreadNoticeList.postValue(unreadUserNoticeList)
            _totalUnreadMsgCount.postValue(unreadUserNoticeList.size)
            return
        }

        unreadUserNoticeList.forEach { unReadMsg->
            if (noticeList.find { it.id == unReadMsg.id } == null) {
                noticeList.add(unReadMsg)
            }
        }

        _unreadNoticeList.postValue(noticeList!!)
        _totalUnreadMsgCount.postValue(noticeList.size)
    }

    fun clear() {
        _unreadNoticeList.postValue(listOf())
        _totalUnreadMsgCount.postValue(0)
    }

    fun clearList() {
        _readedList.postValue(listOf())
    }
}