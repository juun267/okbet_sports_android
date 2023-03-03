package org.cxct.sportlottery.ui.profileCenter.identity

import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ap.zoloz.hummer.api.*
import kotlinx.android.synthetic.main.fragment_verify_identity_new.*
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.credential.CredentialCompleteData
import org.cxct.sportlottery.network.credential.DocType
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.setTitleLetterSpacing


class VerifyIdentityFragment :
    BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private var transactionId: String? = ""

    private var metaInfo: String = ""

    private val idTypeList = listOf(
        StatusSheetData(DocType.PASSPORT.value, DocType.PASSPORT.showName),
        StatusSheetData(DocType.UM_ID.value, DocType.UM_ID.showName),
        StatusSheetData(DocType.TIN_ID.value, DocType.TIN_ID.showName),
        StatusSheetData(DocType.PHIL_HEALTH_ID.value, DocType.PHIL_HEALTH_ID.showName),
        StatusSheetData(DocType.DRIVE_LICENSE.value, DocType.DRIVE_LICENSE.showName),
        StatusSheetData(DocType.SSS_ID.value, DocType.SSS_ID.showName),
        StatusSheetData(DocType.POSTAL_ID.value, DocType.POSTAL_ID.showName),
        StatusSheetData(DocType.PRC_ID.value, DocType.PRC_ID.showName),
        StatusSheetData(DocType.VOTER_ID.value, DocType.VOTER_ID.showName),
        StatusSheetData(DocType.PASSPORT_OLD.value, DocType.PASSPORT_OLD.showName),
        StatusSheetData(DocType.PASSPORT_NEW.value, DocType.PASSPORT_NEW.showName)
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
        (activity as VerifyIdentityActivity).setToolBarTitle()
    }

    private fun initView() {
        status_type.tv_name.text = idTypeList[0].showName
        btn_take_photo.setTitleLetterSpacing()
    }

    private fun initOnClick() {
        status_type.tv_name.gravity=Gravity.START or Gravity.CENTER_VERTICAL
        status_type.setItemData(idTypeList as MutableList<StatusSheetData>)
        status_type.setSelectCode(nowSelectCode)
        status_type.setOnItemSelectedListener {
            status_type.tv_name.text = it.showName
            nowSelectCode = it.code
            it.isChecked = true
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
//        btnGallery.setOnClickListener {
//            val action = VerifyIdentityFragmentDirections.actionVerifyIdentityFragmentToCredentialsFragment()
//            findNavController().navigate(action)
//        }

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
                    bizConfig[ZLZConstants.LOCALE] = when(LanguageManager.getSelectLanguage(context)){
                        LanguageManager.Language.ZH -> "zh-CN"
                        LanguageManager.Language.VI -> "vi-VN"
                        else -> "en-US"
                    }
                    bizConfig[ZLZConstants.CHAMELEON_CONFIG_PATH] = "config_realId.zip"
                }
                transactionId = it.data.transactionId

                Handler().postAtFrontOfQueue {
                    ZLZFacade.getInstance().start(request, object : IZLZCallback {
                        override fun onCompleted(response: ZLZResponse) {
                            checkResult(it.data.transactionId)
                        }

                        override fun onInterrupted(response: ZLZResponse) {
                        }
                    })
                }
            }
        }

        viewModel.credentialCompleteResult.observe(viewLifecycleOwner) { event ->
            hideLoading()
            event.getContentIfNotHandled()?.data?.let { data ->
                val isComplete = data.isSuccessed()

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