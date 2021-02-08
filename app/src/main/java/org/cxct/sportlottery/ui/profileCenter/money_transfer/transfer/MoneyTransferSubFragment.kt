package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.custom_spinner.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import kotlinx.android.synthetic.main.fragment_money_transfer_sub.*
import kotlinx.android.synthetic.main.fragment_money_transfer_sub.layout_balance
import kotlinx.android.synthetic.main.view_account_balance_2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.money_transfer.GameData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.TextUtil

class MoneyTransferSubFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val bottomSheetView by lazy { LayoutInflater.from(context).inflate(R.layout.dialog_bottom_sheet_custom, null) }
    private val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(requireContext()) }

//    private val gameDataArg by lazy { MoneyTransferSubFragmentArgs.fromBundle(requireArguments()).gameData }

    private val gameDataArg: MoneyTransferSubFragmentArgs by navArgs()

    var inDataList = mutableListOf<GameData>()
    var outDataList = mutableListOf<GameData>()

    private val rvOutAdapter by lazy {
        SpinnerOutAdapter(SpinnerOutAdapter.ItemCheckedListener { isChecked, data ->
            if (isChecked) {
                out_account.tv_selected.text = data.showName
                out_account.tv_selected.tag = data.code
                bottomSheet.dismiss()
            }
        })
    }

    private val rvInAdapter by lazy {
        SpinnerOutAdapter(SpinnerOutAdapter.ItemCheckedListener { isChecked, data ->
            if (isChecked) {
                in_account.tv_selected.text = data.showName
                in_account.tv_selected.tag = data.code
                bottomSheet.dismiss()
            }
        })
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_money_transfer_sub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setButtonSheet()
        initView()
        initOnclick()
        initObserver()
    }

    private fun setButtonSheet() {
        bottomSheet.setContentView(bottomSheetView)
        //避免bottomSheet與listView的滑動發生衝突
        bottomSheet.behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(@NonNull view: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
        })

    }

    private fun initView() {
        out_account.tv_selected.text = getString(R.string.plat_money)
        in_account.tv_selected.text = gameDataArg.gameData.showName

        out_account.tv_selected.tag = "CG"
        in_account.tv_selected.tag = gameDataArg.gameData.code
    }

    private fun initOnclick() {
        layout_balance.btn_refresh.setOnClickListener {
            viewModel.getMoney()
        }

        out_account.setOnClickListener {
//            rvOutAdapter.dataList = outDataList
            bottomSheetView.spinner_rv_more.adapter = rvOutAdapter
            bottomSheet.show()
        }

        in_account.setOnClickListener {
//            rvInAdapter.dataList = inDataList
            bottomSheetView.spinner_rv_more.adapter = rvInAdapter
            bottomSheet.show()
        }

        btn_transfer.setOnClickListener {
            viewModel.transfer(out_account.tv_selected.tag.toString(), in_account.tv_selected.tag.toString(), et_transfer_money.getText().toLongOrNull())
        }

    }

    private fun initObserver() {
        receiver.userMoney.observe(viewLifecycleOwner) {
            it?.apply {
                layout_balance.tv_account_balance.text = TextUtil.format(it)
            }
        }

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.apply {
                layout_balance.tv_account_balance.text = TextUtil.format(it)
            }
        }

        viewModel.allBalanceResultList.observe(viewLifecycleOwner) {

            if (it == null) return@observe

            val list = mutableListOf<GameData>()
            it.forEach { gameData ->
                list.add(gameData)
            }

            list.add(0, GameData().apply {
                code = "CG"
                showName = getString(R.string.plat_money)
            })

            inDataList = list
            outDataList = list

            rvInAdapter.dataList = list
            rvOutAdapter.dataList = list

        }

        viewModel.transferResult.observe(viewLifecycleOwner) {
            it?.apply {
                    val dialog = CustomAlertDialog(requireActivity()).apply {
                        setTitle(getString(R.string.prompt))
                        setMessage(it.msg)
                        setNegativeButtonText(null)
                        setTextColor(if (it.success) R.color.gray6 else R.color.red2)
                    }
                    dialog.show()
                view?.findNavController()?.navigate(MoneyTransferSubFragmentDirections.actionMoneyTransferSubFragmentToMoneyTransferFragment())
            }
        }

    }


}
