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
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomSheetFragment
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.setWebViewCommonBackgroundColor


/**
 * @author kevin
 * @create 2022/3/7
 * @description
 */
@SuppressLint("SetJavaScriptEnabled")
class StatisticsDialog : BaseBottomSheetFragment<StatisticsViewModel>(StatisticsViewModel::class) {

    class StatisticsClickListener(private val onMenuClickListener: () -> Unit) {
        fun onMenuClickListener() = onMenuClickListener.invoke()
    }

    private var mClickListener: StatisticsClickListener? = null

    companion object {
        const val MATCH_ID = "match_id"

        @JvmStatic
        fun newInstance(matchId: String?, clickListener: StatisticsClickListener) = StatisticsDialog().apply {
            arguments = Bundle().apply {
                putString(MATCH_ID, matchId)
                mClickListener = clickListener
            }
        }
    }


    private lateinit var vBinding: DialogBottomSheetWebviewBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        vBinding = DialogBottomSheetWebviewBinding.inflate(inflater, container, false)
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
        vBinding.sheetTvTitle.text = context?.getString(R.string.statistics_title)
    }


    private fun setupToolbar() {
        vBinding.gameToolbar.toolBar.visibility = View.VISIBLE
    }


    private fun setupListener() {
        vBinding.ivClose.setOnClickListener {
            dismiss()
        }

        vBinding.gameToolbar.btnLogin.setOnClickListener {
            dismiss()
            startActivity(Intent(MultiLanguagesApplication.appContext, LoginActivity::class.java))
        }

        vBinding.gameToolbar.btnRegister.setOnClickListener {
            dismiss()
            startActivity(Intent(MultiLanguagesApplication.appContext,
                RegisterOkActivity::class.java))
        }

        vBinding.gameToolbar.ivLogo.setOnClickListener {
            dismiss()
//            if (sConfigData?.thirdOpen == FLAG_OPEN) {
//                MainActivity.reStart(MultiLanguagesApplication.appContext)
//            } else {
                GamePublicityActivity.reStart(MultiLanguagesApplication.appContext)
//            }
        }

        vBinding.gameToolbar.ivNotice.setOnClickListener {
            dismiss()
            startActivity(
                Intent(MultiLanguagesApplication.appContext, InfoCenterActivity::class.java)
                    .putExtra(InfoCenterActivity.KEY_READ_PAGE, InfoCenterActivity.YET_READ)
            )
        }

        vBinding.gameToolbar.ivMenu.setOnClickListener {
            dismiss()
            mClickListener?.onMenuClickListener()
        }
    }


    private fun setupWebView() {
        dialog?.findViewById<View>(R.id.design_bottom_sheet)?.let {
            it.setBackgroundResource(android.R.color.transparent)
            it.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            BottomSheetBehavior.from<View>(it).apply {
                //扣除tool bar及status bar的高度
                peekHeight = resources.displayMetrics.heightPixels
                isHideable = false
            }
        }

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

        viewModel.infoCenterRepository.unreadNoticeList.observe(viewLifecycleOwner, {
            vBinding.gameToolbar.ivNotice.setImageResource(if (it.isNotEmpty()) R.drawable.icon_bell_with_red_dot else R.drawable.icon_bell)
        })
    }

    private fun updateUiWithLogin(isLogin: Boolean) {
        with(vBinding.gameToolbar) {
            if (isLogin) {
                btnLogin.visibility = View.GONE
                ivMenu.visibility = View.VISIBLE
                ivNotice.visibility = View.VISIBLE
                btnRegister.visibility = View.GONE
                toolbarDivider.visibility = View.GONE
                ivHead.visibility = View.GONE
                tvOddsType.visibility = View.GONE
            } else {
                btnLogin.visibility = View.VISIBLE
                btnRegister.visibility = View.VISIBLE
                toolbarDivider.visibility = View.VISIBLE
                ivHead.visibility = View.GONE
                tvOddsType.visibility = View.GONE
                ivMenu.visibility = View.GONE
                ivNotice.visibility = View.GONE
            }
        }
    }

}