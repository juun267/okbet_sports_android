package org.cxct.sportlottery.ui.favorite

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.sport.favorite.FavoriteFragment

class FavoriteActivity : BaseSocketActivity<MainViewModel>(MainViewModel::class) {

    private val contentId = R.id.frameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LinearLayout(this).apply {
            addView(FrameLayout(this@FavoriteActivity).also {
                it.id = contentId
            })
        })
        supportFragmentManager.beginTransaction().add(contentId, FavoriteFragment.newInstance())
            .commitAllowingStateLoss()
    }
}