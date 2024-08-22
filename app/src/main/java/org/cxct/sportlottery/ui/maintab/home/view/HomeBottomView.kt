package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewHomeBottomBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.maintab.home.bettingstation.BettingStationActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import splitties.systemservices.layoutInflater

class HomeBottomView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle) {

    val binding = ViewHomeBottomBinding.inflate(layoutInflater,this)
    init {
        orientation = VERTICAL
        initView()
    }

    private fun initView() =binding.run{
        textView17.text = Constants.copyRightString
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
        tvBettingStation.clickDelay{
            if (AppManager.currentActivity() is BettingStationActivity) return@clickDelay
            context.startActivity(Intent(context,BettingStationActivity::class.java))
        }
        jumpToWebView(
            findViewById(R.id.textView16),
            Constants.getDutyRuleUrl(context),
            R.string.responsible
        )

        jumpToWebView(tvFaqs, Constants.getFAQsUrl(context), R.string.faqs)
        initRcvPaymentMethod(mutableListOf(
            R.drawable.icon_gcash,
            R.drawable.icon_paymaya,
            R.drawable.icon_fortune_pay,
            R.drawable.icon_dragonpay_logo,
//            R.drawable.icon_rbank_logo,
//            R.drawable.icon_epon,
            R.drawable.icon_unionbank,
            R.drawable.icon_aub,
            R.drawable.icon_payloro,
        ), rcvPayment)

        initRcvPaymentMethod(mutableListOf(
            R.drawable.icon_bpi_logo,
            R.drawable.icon_ussc_logo,
        ), rcvPayment2)

        val serviceEmail = sConfigData?.customerServiceEmailAddress
        if (!serviceEmail.isNullOrEmpty()) {
            tvEmail.visible()
            tvEmail.setOnClickListener { toSendEmail(it.context, serviceEmail) }
        }
    }

    private fun jumpToWebView(view: View, url: String, @StringRes title: Int) {
        view.setOnClickListener {
            val context = view.context
            JumpUtil.toInternalWeb(context, url, context.getString(title))
        }
    }

    private fun initRcvPaymentMethod(datas: MutableList<Int>, rcvPayment: RecyclerView) {
        rcvPayment.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
        rcvPayment.addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_8))
        val paymentAdapter = object : BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_view_payment_method) {
            val lp = LayoutParams(-2, 44.dp)
            override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val iv = ImageView(parent.context)
                iv.layoutParams = lp
                iv.scaleType = ImageView.ScaleType.CENTER
                return BaseViewHolder(iv)
            }

            override fun convert(holder: BaseViewHolder, item: Int) {
                (holder.itemView as ImageView).setImageResource(item)
            }

        }
        rcvPayment.adapter = paymentAdapter
        paymentAdapter.setNewInstance(datas)
    }

    fun bindServiceClick(fragmentManager: FragmentManager) = binding.run{
        tvLiveChat.setServiceClick(fragmentManager)
        tvContactUs.setServiceClick(fragmentManager)
    }

}