package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.playquotacom.PlayQuotaComResult
import retrofit2.Response

class PlayQuotaComRepository {


    val _playquotacom = MutableLiveData<PlayQuotaComResult>()
    val playQuotaCom: LiveData<PlayQuotaComResult>
        get() = _playquotacom


    suspend fun getPlayQuotaCom(): Response<PlayQuotaComResult> {
        return OneBoSportApi.playQuotaComService.playQuotaComList()
    }


    fun postPlayQuotaCom(result: PlayQuotaComResult){
        _playquotacom.postValue(result)
    }


}

