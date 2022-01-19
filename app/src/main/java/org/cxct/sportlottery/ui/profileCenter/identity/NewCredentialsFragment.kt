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
import kotlinx.android.synthetic.main.fragment_credentials_new.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.credential.DocType
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.home.HomeFragmentDirections
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import java.util.*


open class NewCredentialsFragment : BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

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
        val metaInfo = ZLZFacade.getMetaInfo(activity)

        btn_take_photo.setOnClickListener {
            viewModel.getCredentialInitial(metaInfo, DocType.Passport.value)
        }

    }
    private val request by lazy {  ZLZRequest() }
/*
    val handler by lazy {
        Handler()
    }
*/

    private fun initObserve() {

        viewModel.credentialInitialResult.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                request.apply {
                    zlzConfig = it.data.clientCfg
                    bizConfig[ZLZConstants.CONTEXT] = context
                    bizConfig[ZLZConstants.PUBLIC_KEY] = it.data.rsaPubKey
                    bizConfig[ZLZConstants.LOCALE] = Locale.getDefault()
                }

                Handler().postAtFrontOfQueue {
                    ZLZFacade.getInstance().start(request, object : IZLZCallback {
                        override fun onCompleted(response: ZLZResponse) {
//                            val action = NewCredentialsFragmentDirections.actionCredentialsFragmentToNewCredentialsDetailFragment(it.data.transactionId)
//                            findNavController().navigate(action)
                        }

                        override fun onInterrupted(response: ZLZResponse) {
                            Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }

    }

}