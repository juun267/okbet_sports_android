package org.cxct.sportlottery.ui.profileCenter.pointshop

import android.app.Application
import org.cxct.sportlottery.net.point.PointRepository
import org.cxct.sportlottery.ui.base.BaseSocketViewModel

class PointRuleViewModel(androidContext: Application) : BaseSocketViewModel(androidContext) {
    val pointRule get() = PointRepository.pointRule
}