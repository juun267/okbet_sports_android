package org.cxct.sportlottery.ui.profileCenter.identity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel

class IdentityActivity : BaseSocketActivity<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private val mNavController by lazy { findNavController(R.id.identity_container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity)

        initToolbar()
    }

    private fun initToolbar() {
        tv_toolbar_title.text = getString(R.string.identity)
            btn_toolbar_back.setOnClickListener{
                if (mNavController.previousBackStackEntry == null)
                    finish()
                else
                    mNavController.popBackStack()
            }
    }
}