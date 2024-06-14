package org.cxct.sportlottery.ui.profileCenter.invite

import org.cxct.sportlottery.databinding.ActivityInviteBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel
import org.cxct.sportlottery.util.ShareUtil

class InviteActivity : BaseActivity<ProfileModel, ActivityInviteBinding>(){

    override fun onInitView()=binding.run {
        btnFacebook.setOnClickListener {
            ShareUtil.shareFacebook(this@InviteActivity,"share to facebook https://www.okbet.com/")
        }
        btnMessenger.setOnClickListener {
            ShareUtil.shareMessenger(this@InviteActivity,"share to messenger https://www.okbet.com/")
        }
        btnInstagram.setOnClickListener {
            ShareUtil.shareInstagram(this@InviteActivity, "share to instagram")
        }
        btnSystem.setOnClickListener {
            ShareUtil.shareBySystem(this@InviteActivity, "share by system")
        }
        btnViber.setOnClickListener {
            ShareUtil.shareViber(this@InviteActivity, "how are you! https://www.okbet.com/")
        }
        btnSms.setOnClickListener {
            ShareUtil.sendSMS(this@InviteActivity, "how are you! https://www.okbet.com/")
        }
        btnSms.setOnClickListener {
            ShareUtil.sendSMS(this@InviteActivity, "how are you! https://www.okbet.com/")
        }
        btnShare.setOnClickListener {
            showShareDialog()
        }
    }
    private fun showShareDialog(){
        InviteDialog().show(supportFragmentManager)
    }

}