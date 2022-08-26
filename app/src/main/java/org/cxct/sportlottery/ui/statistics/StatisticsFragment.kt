package org.cxct.sportlottery.ui.statistics


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBottomSheetWebviewBinding
import org.cxct.sportlottery.databinding.DialogBottomSheetWebviewDetailBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.setWebViewCommonBackgroundColor


/**
 * @author kevin
 * @create 2022/3/7
 * @description
 */
@SuppressLint("SetJavaScriptEnabled")
class StatisticsFragment : BaseFragment<StatisticsViewModel>(StatisticsViewModel::class) {

    class StatisticsClickListener(private val onMenuClickListener: () -> Unit) {
        fun onMenuClickListener() = onMenuClickListener.invoke()
    }

//    private var mClickListener: StatisticsClickListener? = null

    companion object {
        const val MATCH_ID = "match_id"

        @JvmStatic
        fun newInstance(matchId: String?) = StatisticsFragment().apply {
            arguments = Bundle().apply {
                putString(MATCH_ID, matchId)
//                mClickListener = clickListener
            }
        }
    }


    private lateinit var vBinding: DialogBottomSheetWebviewDetailBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        vBinding = DialogBottomSheetWebviewDetailBinding.inflate(inflater, container, false)
        return vBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTitle()
        setupToolbar()
        setupListener()
        setupWebView()
        initObserver()
    }


    private fun setupTitle() {
    }


    private fun setupToolbar() {
    }


    private fun setupListener() {

    }


    private fun setupWebView() {
        //TODO 暂时隐藏
//        dialog?.findViewById<View>(R.id.design_bottom_sheet)?.let {
//            it.setBackgroundResource(android.R.color.transparent)
//            it.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
//            BottomSheetBehavior.from<View>(it).apply {
//                //扣除tool bar及status bar的高度
//                peekHeight = resources.displayMetrics.heightPixels
//                isHideable = false
//            }
//        }

        vBinding.bottomSheetWebView.apply {
            settings.javaScriptEnabled = true

            webViewClient = WebViewClient()

            sConfigData?.analysisUrl?.replace("{lang}", LanguageManager.getSelectLanguage(context).key)
                ?.replace("{eventId}", arguments?.getString(MATCH_ID) ?: "")?.let {
                    loadUrl(it)
                }
            setWebViewCommonBackgroundColor()
        }
    }

    private fun initObserver() {
        viewModel.isLogin.observe(viewLifecycleOwner, {
            updateUiWithLogin(it)
        })
    }

    private fun updateUiWithLogin(isLogin: Boolean) {
    }

    fun dismiss() {
    }
}