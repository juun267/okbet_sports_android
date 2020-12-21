package org.cxct.sportlottery.ui.odds

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.odds.OddsDetailResult
import org.cxct.sportlottery.network.playcate.PlayCateListResult
import org.cxct.sportlottery.repository.OddsRepository
import org.cxct.sportlottery.repository.PlayCateListRepository
import org.cxct.sportlottery.util.TimeUtil

class OddsDetailViewModel(private val oddsRepository: OddsRepository, private val playCateListRepository: PlayCateListRepository) : ViewModel() {

    val oddsDetailResult = MutableLiveData<OddsDetailResult?>()

    val playCateListResult = MutableLiveData<PlayCateListResult?>()

    fun getOddsDetail(matchId: String, oddsType: String) {
        viewModelScope.launch {
            oddsDetailResult.postValue(oddsRepository.getOddsDetail(matchId, oddsType))
        }
    }

    fun getPlayCateList(gameType: String) {
        viewModelScope.launch {
            playCateListResult.postValue(playCateListRepository.getPlayCateList(gameType))
        }
    }

}