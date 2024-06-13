package org.cxct.sportlottery.ui.profileCenter.share

import org.cxct.sportlottery.databinding.ActivityShareBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel
import org.cxct.sportlottery.util.ShareUtil

class ShareActivity : BaseActivity<ProfileModel,ActivityShareBinding>(){
    override fun onInitView()=binding.run {
        btnFacebook.setOnClickListener {
            ShareUtil.shareFacebook(this@ShareActivity,"share to facebook https://www.okbet.com/")
        }
        btnMessenger.setOnClickListener {
            ShareUtil.shareMessenger(this@ShareActivity,"share to messenger https://www.okbet.com/")
        }
        btnInstagram.setOnClickListener {
            ShareUtil.shareInstagram(this@ShareActivity, "share to instagram")
        }
        btnSystem.setOnClickListener {
            ShareUtil.shareBySystem(this@ShareActivity, "share by system")
        }
        btnViber.setOnClickListener {
            ShareUtil.shareViber(this@ShareActivity, "how are you! https://www.okbet.com/")
        }
        btnSms.setOnClickListener {
            ShareUtil.sendSMS(this@ShareActivity, "how are you! https://www.okbet.com/")
        }
    }

}