package org.cxct.sportlottery.ui.favorite

import android.content.Context
import android.widget.FrameLayout
import org.cxct.sportlottery.R
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.add
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent

class FavoriteUi(override val ctx: Context) : Ui {

    override val root: FrameLayout = frameLayout {
        add(
            frameLayout(id = R.id.frameLayout),
            lParams(width = matchParent, height = matchParent)
        )
        add(
            frameLayout(id = R.id.fl_bet_list),
            lParams(width = matchParent, height = matchParent)
        )
    }

}