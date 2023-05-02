package org.cxct.sportlottery.ui.maintab.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setServiceClick

class HomeButtomView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : ConstraintLayout(context, attrs, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.include_view_payment_method, this, true)
        bindView()
    }

    private fun bindView() {
        setPadding(0, 0, 0, 40.dp)
        val tvPrivacyPolicy = findViewById<TextView>(R.id.tvPrivacyPolicy)
        val tvTermConditions = findViewById<TextView>(R.id.tvTermConditions)
        val tvResponsibleGaming = findViewById<TextView>(R.id.tvResponsibleGaming)
        val tvFaqs = findViewById<TextView>(R.id.tvFaqs)
        val rcvPayment = findViewById<RecyclerView>(R.id.rcvPayment)

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
            R.drawable.icon_epon
        )
        rcvPayment.layoutManager = GridLayoutManager(context, 4)
        val paymentAdapter =
            object : BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_view_payment_method) {
                override fun convert(holder: BaseViewHolder, item: Int) {
                    holder.setImageResource(R.id.iv, item)
                }

            }
        rcvPayment.adapter = paymentAdapter
        paymentAdapter.setNewInstance(list)
    }

    fun bindServiceClick(fragmentManager: FragmentManager) {
        findViewById<View>(R.id.tvLiveChat).setServiceClick(fragmentManager)
        findViewById<View>(R.id.tvContactUs).setServiceClick(fragmentManager)
    }
}