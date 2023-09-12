package org.cxct.sportlottery.common.event

import android.view.Gravity


data class MenuEvent(var open: Boolean,val gravity: Int = Gravity.LEFT)