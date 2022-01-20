package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.ap.zoloz.hummer.api.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_credentials_detail.*
import kotlinx.android.synthetic.main.fragment_credentials_new.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.credential.CredentialCompleteData
import org.cxct.sportlottery.network.credential.DocType
import org.cxct.sportlottery.network.credential.EkycResultType
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity


open class CredentialsFragment :
    BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

//    private var isComplete = false

    private var transactionId: String? = ""

    private var metaInfo: String = ""

    private val request by lazy { ZLZRequest() }

    private val mNavController by lazy {
        findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_credentials_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBtn()
        initObserve()
    }

    private fun initBtn() {
        kotlin.run {
            metaInfo = ZLZFacade.getMetaInfo(activity)
        }

        btn_take_photo.setOnClickListener {
            if (metaInfo.isNotEmpty()) viewModel.getCredentialInitial(
                metaInfo,
                DocType.Passport.value
            )
        }

    }

    override fun onResume() {
        super.onResume()
//        if (isComplete) changePage()
//        else showPromptDialog(getString(R.string.prompt), getString(R.string.verify_failed_please_retry)) {}

    }

    private fun changePage(data: CredentialCompleteData) {
        val action =
            CredentialsFragmentDirections.actionCredentialsFragmentToCredentialsDetailFragment(
                data
            )
        mNavController.navigate(action)
    }

    private fun initObserve() {

        viewModel.credentialInitialResult.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                request.apply {
                    zlzConfig = it.data.clientCfg
                    bizConfig[ZLZConstants.CONTEXT] = context
                    bizConfig[ZLZConstants.PUBLIC_KEY] = it.data.rsaPubKey
                    bizConfig[ZLZConstants.LOCALE] = "en-US"
                }
                transactionId = it.data.transactionId

                Handler().postAtFrontOfQueue {
                    ZLZFacade.getInstance().start(request, object : IZLZCallback {
                        override fun onCompleted(response: ZLZResponse) {
                            checkResult(it.data.transactionId)
                        }

                        override fun onInterrupted(response: ZLZResponse) {
                            showPromptDialog(getString(R.string.prompt), getString(R.string.verify_failed_please_retry)){}
                        }
                    })
                }
            }
        }

        viewModel.credentialCompleteResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.data?.let { data ->
                val isComplete = data.ekycResult == EkycResultType.SUCCESS.value
                        && data.extFaceInfo?.ekycResultFace == EkycResultType.SUCCESS.value
                        && data.extIdInfo?.ekycResultDoc == EkycResultType.SUCCESS.value

                //測試用
//                Log.e(">>>", "${data.ekycResult == EkycResultType.SUCCESS.value}, " +
//                        "${data.extFaceInfo?.ekycResultFace == EkycResultType.SUCCESS.value}, " +
//                        "${data.extIdInfo?.ekycResultDoc == EkycResultType.SUCCESS.value} ")
                if (isComplete) changePage(data)
                else showPromptDialog(getString(R.string.prompt), getString(R.string.verify_failed_please_retry)) {}
            }
        }



    }

    private fun checkResult(transactionId: String?) {
        viewModel.getCredentialCompleteResult(transactionId)
    }
}