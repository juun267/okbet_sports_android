package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


class InfoCenterActivity : BaseActivity<InfoCenterViewModel>(InfoCenterViewModel::class) {

    private val InfoCenterViewModel: InfoCenterViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_center)
    }
}