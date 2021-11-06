package org.cxct.sportlottery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_credentials_detail.*
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel

class CredentialsDetailFragment : BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_credentials_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_submit.setOnClickListener {
            showPromptDialog(getString(R.string.prompt), "完成") { activity?.finish() }
        }
    }
}