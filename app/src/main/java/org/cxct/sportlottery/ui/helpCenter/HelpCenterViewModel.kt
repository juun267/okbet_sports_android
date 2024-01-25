package org.cxct.sportlottery.ui.helpCenter

import android.app.Application
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel

class HelpCenterViewModel(
    androidContext: Application
) : BaseViewModel(
    androidContext
) {
    val token = LoginRepository.token
}