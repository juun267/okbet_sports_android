package org.cxct.sportlottery.ui.odds

import androidx.lifecycle.LiveData
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

    private val _playCateListResult = MutableLiveData<PlayCateListResult?>()

    private val _oddsDetailResult = MutableLiveData<OddsDetailResult?>()

    val playCateListResult: LiveData<PlayCateListResult?>
        get() = _playCateListResult

    val oddsDetailResult: LiveData<OddsDetailResult?>
        get() = _oddsDetailResult


    fun getOddsDetail(matchId: String, oddsType: String) {
        viewModelScope.launch {
            _oddsDetailResult.postValue(oddsRepository.getOddsDetail(matchId, oddsType))
        }
    }

    fun getPlayCateList(gameType: String) {
        viewModelScope.launch {
            _playCateListResult.postValue(playCateListRepository.getPlayCateList(gameType))
        }
    }

}