package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ap.zoloz.hummer.api.*
import kotlinx.android.synthetic.main.fragment_verify_identity_new.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.credential.CredentialCompleteData
import org.cxct.sportlottery.network.credential.DocType
import org.cxct.sportlottery.network.credential.ResultStatus
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel


class VerifyIdentityFragment :
    BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private var transactionId: String? = ""

    private var metaInfo: String = ""

    private val idTypeList = listOf(
        StatusSheetData(DocType.UM_ID.value, DocType.UM_ID.showName),
        StatusSheetData(DocType.TIN_ID.value, DocType.TIN_ID.showName),
        StatusSheetData(DocType.PASSPORT.value, DocType.PASSPORT.showName),
        StatusSheetData(DocType.PHIL_HEALTH_CARD.value, DocType.PHIL_HEALTH_CARD.showName),
        StatusSheetData(DocType.DRIVE_LICENSE.value, DocType.DRIVE_LICENSE.showName),
        StatusSheetData(DocType.SSS_ID.value, DocType.SSS_ID.showName),
        StatusSheetData(DocType.VOTER_ID.value, DocType.VOTER_ID.showName),
        StatusSheetData(DocType.HKID_1.value, DocType.HKID_1.showName),
        StatusSheetData(DocType.HKID_2.value, DocType.HKID_2.showName),
        StatusSheetData(DocType.MY_KAD.value, DocType.MY_KAD.showName),
        StatusSheetData(DocType.CHINA_ID.value, DocType.CHINA_ID.showName)
    )

    private var nowSelectCode: String? = null

    private val request by lazy { ZLZRequest() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verify_identity_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kotlin.run {
            metaInfo = ZLZFacade.getMetaInfo(activity)
        }

        initView()
        initOnClick()
        initObserve()
    }

    private fun initView() {
        tv_select_bank_card.text = idTypeList[0].showName
    }

    private fun initOnClick() {

        ll_select_credential.setOnClickListener {
            showBottomSheetDialog(
                null,
                idTypeList,
                idTypeList[0],
                StatusSheetAdapter.ItemCheckedListener { _, data ->
                    tv_select_bank_card.text = data.showName
                    nowSelectCode = data.code
                })
        }
        btn_take_photo.setOnClickListener {
            if (metaInfo.isEmpty()) {
                showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.error)
                ) {}
            } else {
                loading()
                viewModel.getCredentialInitial(
                    metaInfo,
                    nowSelectCode ?: DocType.PASSPORT.value
                )
            }
        }

    }

    private fun changePage(data: CredentialCompleteData) {
        kotlin.run {
            val action =
                VerifyIdentityFragmentDirections.actionVerifyIdentityFragmentToVerifyDetailFragment(
                    data
                )
            findNavController().navigate(action)
        }
    }

    private fun initObserve() {

        viewModel.credentialInitialResult.observe(viewLifecycleOwner) { event ->
            hideLoading()
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
                            showPromptDialog(
                                getString(R.string.prompt),
                                getString(R.string.error)
                            ) {}
                        }
                    })
                }
            }
        }

        viewModel.credentialCompleteResult.observe(viewLifecycleOwner) { event ->
            hideLoading()
            event.getContentIfNotHandled()?.data?.let { data ->
                val isComplete = data.result?.resultStatus == ResultStatus.SUCCESS.value

                if (isComplete) changePage(data)
                else showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.verify_failed_please_retry)
                ) {}
            }
        }


    }

    private fun checkResult(transactionId: String?) {
        loading()
        viewModel.getCredentialCompleteResult(transactionId)
    }
}