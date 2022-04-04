package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.config.VerifySwitchType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel

class VerifyIdentityActivity :
    BaseSocketActivity<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private val mNavController by lazy { findNavController(R.id.identity_container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_identity)

        initToolbar()

    }

    override fun onStart() {
        super.onStart()
        checkKYCEnable()
    }

    private fun checkKYCEnable() {
        if (sConfigData?.enableKYCVerify == VerifySwitchType.CLOSE.value) {
            when (mNavController.currentDestination?.id) {
                R.id.verifyIdentityFragment -> {
                    val action = VerifyIdentityFragmentDirections.actionVerifyIdentityFragmentToCredentialsFragment()
                    mNavController.navigate(action)
                }
            }
        }
    }

    private fun initToolbar() {
        tv_toolbar_title.text = getString(R.string.select_id_type)
        btn_toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }


    fun setToolBar(title:String){
        tv_toolbar_title.text = title
    }
    
    fun setToolBarTitleForDetail(){
        tv_toolbar_title.text = getString(R.string.scan_tool_bar)
    }

    fun setToolBarTitle(){
        tv_toolbar_title.text = getString(R.string.select_id_type)
    }

    override fun onBackPressed() {
        when (mNavController.currentDestination?.id) {
            R.id.verifyIdentityFragment -> {
                finish()
            }
            R.id.credentialsFragment -> {
                finish()
            }
        }
        super.onBackPressed()
    }
}