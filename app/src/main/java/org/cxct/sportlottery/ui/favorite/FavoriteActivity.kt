package org.cxct.sportlottery.ui.favorite

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.addFragment
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.sport.favorite.FavoriteFragment
import splitties.views.dsl.core.frameLayout

class FavoriteActivity : BaseSocketActivity<MainViewModel>(MainViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentId = R.id.frameLayout
        setContentView(frameLayout(contentId))
        addFragment(contentId, FavoriteFragment.newInstance())
    }
}