package org.cxct.sportlottery.ui.money.recharge

import android.annotation.SuppressLint
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.online_pay_fragment.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.CustomImageAdapter
import org.cxct.sportlottery.util.MoneyManager

class OnlinePayFragment : BaseFragment<MoneyRechViewModel>(MoneyRechViewModel::class) {

    companion object {
        private const val TAG = "OnlinePayFragment"
    }

    private var mMoneyPayWay: MoneyPayWayData? = MoneyPayWayData("", "", "", "", 0) //支付類型

    private var mSelectRechCfgs: MoneyRechCfg.RechConfig? = null //選擇的入款帳號

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.online_pay_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPayRoadSpinner()
        refreshPayBank()
    }

    fun setArguments(moneyPayWay: MoneyPayWayData?): OnlinePayFragment {
        mMoneyPayWay = moneyPayWay
        return this
    }

    private fun initPayRoadSpinner() {
        //支付類型的入款帳號清單
        val rechCfgsList = viewModel.rechargeConfigs.value?.rechCfgs?.filter {
            it.rechType == mMoneyPayWay?.rechType && it.onlineType == mMoneyPayWay?.onlineType && it.pcMobile != 1
        } ?: mutableListOf()

        //產生對應 spinner 選單
        var count = 1

        val payRoadSpannerList = mutableListOf<String>()
        val title = mMoneyPayWay?.title

        if (rechCfgsList.size > 1)
            rechCfgsList.forEach { _ -> payRoadSpannerList.add(title + count++) }
        else
            rechCfgsList.forEach { _ -> payRoadSpannerList.add(title + "") }

        sp_pay_gap.adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_text_item, payRoadSpannerList)

        //選擇入款帳號
        sp_pay_gap.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                try {
//                    sp_pay_gap.text = sp_pay_gap.selectedItem.toString()
                    mSelectRechCfgs = rechCfgsList[position]
                    refreshSelectRechCfgs(mSelectRechCfgs)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        //default 選擇第一個不為 null 的入款帳號資料
        if (sp_pay_gap.count > 0)
            sp_pay_gap.setSelection(0)
        else
            mSelectRechCfgs = null
    }

    //依據選擇的支付渠道，刷新UI
    @SuppressLint("SetTextI18n")
    private fun refreshSelectRechCfgs(selectRechCfgs: MoneyRechCfg.RechConfig?) {
        //手續費率/贈送費率 <0是手續費 >0是贈送費率 PM還在討論
        txv_nickname.text = "${selectRechCfgs?.rebateFee.toString()}%"
        et_recharge_money.hint = String.format(
            getString(R.string.edt_hint_online_pay_money),
            "${selectRechCfgs?.minMoney}",
            "${selectRechCfgs?.maxMoney}"
        )
    }
    //還不知道篩選條件是什麼
    private fun refreshPayBank(){

        val spannerList = mutableListOf<CustomImageAdapter.SelectBank>()
        viewModel.rechargeConfigs.value?.banks?.forEach {
            var data = CustomImageAdapter.SelectBank(it.name,MoneyManager.getBankIcon(it.name.toString()))
            spannerList.add(data)
        }
        sp_pay_bank.adapter = CustomImageAdapter(context, spannerList)
//
//        sp_pay_account.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                try {
//                  //TODO Bill
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
    }


}