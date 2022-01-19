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
import com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread
import kotlinx.android.synthetic.main.fragment_credentials_new.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.credential.DocType
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel


open class NewCredentialsFragment :
    BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private var isComplete = false

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
            Log.e(">>>", "metaInfo = $metaInfo")

//            val action = NewCredentialsFragmentDirections.actionCredentialsFragmentToNewCredentialsDetailFragment()
//            findNavController().navigate(action)
            if (metaInfo.isNotEmpty()) viewModel.getCredentialInitial(
                metaInfo,
                DocType.Passport.value
            )
        }

    }

    override fun onResume() {
        super.onResume()
        if (isComplete) changePage()
    }

    private fun changePage() {
        val action =
            NewCredentialsFragmentDirections.actionCredentialsFragmentToNewCredentialsDetailFragment(
                transactionId
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
                            isComplete = true
                        }

                        override fun onInterrupted(response: ZLZResponse) {
                            Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
                }
            }
        }

    }
}