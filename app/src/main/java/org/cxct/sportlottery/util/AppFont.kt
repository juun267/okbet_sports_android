package org.cxct.sportlottery.util

import androidx.core.content.res.ResourcesCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication

object AppFont {

    val helvetica by lazy { ResourcesCompat.getFont(MultiLanguagesApplication.appContext, R.font.helvetica_normal)!! }

    val inter_bold by lazy { ResourcesCompat.getFont(MultiLanguagesApplication.appContext, R.font.inter_bold)!! }

    val regular by lazy { ResourcesCompat.getFont(MultiLanguagesApplication.appContext, R.font.inter_regular)!! }

    val helvetica_bold by lazy { ResourcesCompat.getFont(MultiLanguagesApplication.appContext, R.font.helvetica_bold)!! }
}