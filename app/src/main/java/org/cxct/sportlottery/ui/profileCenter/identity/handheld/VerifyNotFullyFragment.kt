package org.cxct.sportlottery.ui.profileCenter.identity.handheld

import android.content.Intent
import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentVerifyNotFullyBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity

class VerifyNotFullyFragment: BaseFragment<ProfileCenterViewModel, FragmentVerifyNotFullyBinding>() {

    override fun onInitView(view: View) {
        binding.btnStart.setOnClickListener {
            startActivity(Intent(requireContext(),VerifyHandheldActivity::class.java))
        }
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        (requireActivity() as VerifyIdentityActivity).setToolBar(getString(R.string.identity))
    }


}