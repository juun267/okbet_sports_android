package org.cxct.sportlottery.ui.profileCenter.identity.handheld

import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.ActivityVerifyHandheldBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel

class VerifyHandheldActivity: BaseActivity<ProfileCenterViewModel, ActivityVerifyHandheldBinding>() {

    override fun pageName(): String {
        return "确认上传手持照片"
    }

    override fun onInitView() {
        setStatusbar(darkFont = true)
        binding.customToolBar.setOnBackPressListener {
            finish()
        }
        binding.btnStart.setOnClickListener {
            startActivity<HandheldPhotoActivity>()
            finish()
        }
    }



}