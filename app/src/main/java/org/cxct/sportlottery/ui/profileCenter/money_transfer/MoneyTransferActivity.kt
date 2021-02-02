package org.cxct.sportlottery.ui.profileCenter.money_transfer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class MoneyTransferActivity : BaseToolBarActivity<MoneyTransferViewModel>(MoneyTransferViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(): Int {
        return R.layout.activity_money_transfer
    }

    override fun setToolBarName(): String {
        return getString(R.string.account_transfer)
    }
}