package org.cxct.sportlottery.ui.withdraw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class WithdrawActivity : BaseToolBarActivity<WithdrawViewModel>(WithdrawViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)
    }

    override fun setContentView(): Int {
        return R.layout.activity_withdraw
    }

    override fun setToolBarName(): String {
        return getString(R.string.withdraw)
    }
}