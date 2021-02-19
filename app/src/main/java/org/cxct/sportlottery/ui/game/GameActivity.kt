package org.cxct.sportlottery.ui.game

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity

class GameActivity : BaseOddButtonActivity<GameViewModel>(GameViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
    }
}