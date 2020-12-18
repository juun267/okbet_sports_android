package org.cxct.sportlottery.ui.odds

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.odds.OddsDetailResult
import org.cxct.sportlottery.repository.OddsRepository
import org.cxct.sportlottery.util.TimeUtil

class OddsDetailViewModel(private val oddsRepository: OddsRepository) : ViewModel() {

    val oddsDetailResult = MutableLiveData<OddsDetailResult?>()

    fun getOddsDetail(matchId: String, oddsType: String) {
        viewModelScope.launch {
            oddsDetailResult.postValue(oddsRepository.getOddsDetail(matchId, oddsType))
        }
    }

}