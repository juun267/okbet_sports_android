package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewHomeBottomBinding
import org.cxct.sportlottery.databinding.ViewHomeBottomChrisBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.*
import splitties.systemservices.layoutInflater

class HomeBottomView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle) {

    val binding = ViewHomeBottomChrisBinding.inflate(layoutInflater,this)
    init {
        orientation = VERTICAL
        initView()
    }

    private fun initView() =binding.run{
        jumpToWebView(
            tvPrivacyPolicy,
            Constants.getPrivacyRuleUrl(context),
            R.string.privacy_policy
        )
        jumpToWebView(
            tvTermConditions,
            Constants.getAgreementRuleUrl(context),
            R.string.terms_conditions
        )
        jumpToWebView(
            tvResponsibleGaming,
            Constants.getDutyRuleUrl(context),
            R.string.responsible
        )
        jumpToWebView(
            findViewById(R.id.textView16),
            Constants.getDutyRuleUrl(context),
            R.string.responsible
        )

        jumpToWebView(tvFaqs, Constants.getFAQsUrl(context), R.string.faqs)
        initRcvPaymentMethod(rcvPayment)

        val serviceEmail = sConfigData?.customerServiceEmailAddress
        if (!serviceEmail.isNullOrEmpty()) {
            tvEmail.visible()
            tvEmail.setOnClickListener { toSendEmail(it.context, serviceEmail) }
        }
    }

    private fun jumpToWebView(view: View, url: String?, @StringRes title: Int) {
        view.setOnClickListener {
            val context = view.context
            JumpUtil.toInternalWeb(context, url, context.getString(title))
        }
    }

    private fun initRcvPaymentMethod(rcvPayment: RecyclerView) {
        val list = mutableListOf(
            R.drawable.icon_gcash,
            R.drawable.icon_paymaya,
            R.drawable.icon_fortune_pay,
//            R.drawable.icon_epon,
            R.drawable.icon_unionbank,
            R.drawable.icon_aub,
            R.drawable.icon_payloro,
        )
        rcvPayment.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
        rcvPayment.addItemDecoration(SpaceItemDecoration(context,R.dimen.margin_8))
        val paymentAdapter =
            object : BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_view_payment_method) {
                override fun convert(holder: BaseViewHolder, item: Int) {
                    holder.setImageResource(R.id.iv, item)
                }

            }
        rcvPayment.adapter = paymentAdapter
        paymentAdapter.setNewInstance(list)
    }

    fun bindServiceClick(fragmentManager: FragmentManager) = binding.run{
        tvLiveChat.setServiceClick(fragmentManager)
        tvContactUs.setServiceClick(fragmentManager)
    }

    /**
     * 底部显示社交view  首页需要
     */
//    fun showFollowView(){
//        findViewById<HomeFollowView>(R.id.homeFollowView).showFollowView()
//    }

}