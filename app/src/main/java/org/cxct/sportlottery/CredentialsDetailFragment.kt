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
        initFakeData()

    private fun initObserve() {
        viewModel.uploadVerifyPhotoResult.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                if (result.success) {
                    showPromptDialog(getString(R.string.prompt), "完成") { activity?.finish() }
                } else {
                    showErrorPromptDialog(getString(R.string.promotion), result.msg) {}
                }
            }
        })
    }
        btn_submit.setOnClickListener {
            viewModel.uploadIdentityDoc()
        }
        }
    }

    private fun initFakeData() {
        et_identity_id.setText("18101QW1011141710")
        et_identity_last_name.setText("WANG")
        et_identity_first_name.setText("QIBIN")
        et_identity_country.setText("CHINA")
        et_identity_birth.setText("09.Sep.1987")
        et_identity_marital_status.setText("SINGLE")
        et_identity_sex.setText("MALE")
        et_identity_work.setText("WORKER")
    }
}