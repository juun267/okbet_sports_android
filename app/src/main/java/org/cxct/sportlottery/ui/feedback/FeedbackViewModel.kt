package org.cxct.sportlottery.ui.feedback

import android.content.Context
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.FeedbackRepository
import org.cxct.sportlottery.ui.base.BaseViewModel

class FeedbackViewModel(
    private val androidContext: Context,
    private val feedbackRepository: FeedbackRepository,
    betInfoRepo: BetInfoRepository
) : BaseViewModel() {

    init {
        betInfoRepository = betInfoRepo
    }

}