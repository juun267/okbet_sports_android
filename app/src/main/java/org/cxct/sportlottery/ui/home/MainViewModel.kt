package org.cxct.sportlottery.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.match.MatchPreloadRequest
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.ui.base.BaseViewModel


class MainViewModel(
    private val loginRepository: LoginRepository,
    private val sportMenuRepository: SportMenuRepository
) : BaseViewModel() {
    val token: LiveData<String?> by lazy {
        loginRepository.token
    }

    private val _asStartCount = MutableLiveData<Int>()
    val asStartCount: LiveData<Int> //即將開賽的數量
        get() = _asStartCount

    fun logout() {
        loginRepository.logout()
    }

    //獲取系統公告
    fun getAnnouncement(): LiveData<MessageListResult> {
        val messageType = "1"
        return doNetwork {
            OneBoSportApi.messageService.getMessageList(messageType)
        }
    }

    //獲取體育菜單
    fun getSportMenu(): LiveData<SportMenuResult> {
        val liveDataResult = doNetwork {
            sportMenuRepository.getSportMenu()
        }
        val count = liveDataResult.value?.sportMenuData?.atStart?.sumBy { it.num } ?: 0
        _asStartCount.postValue(count)
        return liveDataResult
    }

    //按赛事类型预加载各体育赛事
    fun getMatchPreload(matchType: String): LiveData<MatchPreloadResult> {
        val request = MatchPreloadRequest(matchType)

        return doNetwork {
            OneBoSportApi.matchService.getMatchPreload(request)
        }
    }
}