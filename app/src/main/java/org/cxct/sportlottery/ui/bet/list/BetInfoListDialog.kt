package org.cxct.sportlottery.ui.bet.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_bet_info_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoListBinding
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.base.BaseSocketDialog
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.util.SpaceItemDecoration

class BetInfoListDialog : BaseSocketDialog<GameViewModel>(GameViewModel::class),
    BetInfoListAdapter.OnItemClickListener {


    companion object {
        val TAG = BetInfoListDialog::class.java.simpleName
    }


    private lateinit var binding: DialogBetInfoListBinding


    private lateinit var betInfoListAdapter: BetInfoListAdapter


    private var deletePosition: Int = -1


    init {
        setStyle(R.style.Common)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_bet_info_list, container, false)
        binding.apply {
            gameViewModel = this@BetInfoListDialog.viewModel
            lifecycleOwner = this@BetInfoListDialog.viewLifecycleOwner
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeData()
        initSocketObserver()
    }


    private fun initUI() {
        iv_close.setOnClickListener {
            dismiss()
        }

        betInfoListAdapter = BetInfoListAdapter(requireContext(), this@BetInfoListDialog)

        rv_bet_list.apply {
            adapter = betInfoListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_bet_info_list
                )
            )
        }
    }


    private fun observeData() {
        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, Observer {
            if (it.size == 0) {
                dismiss()
            } else {
                betInfoListAdapter.modify(it, deletePosition)
            }
        })

        viewModel.betAddResult.observe(this.viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                val m: String
                val color: Int
                if (result.success) {
                    m = resources.getString(R.string.bet_info_add_bet_success)
                    color = R.color.gray6
                } else {
                    m = result.msg
                    color = R.color.red2
                }

                val dialog = CustomAlertDialog(requireActivity())
                dialog.setTitle(getString(R.string.prompt))
                dialog.setMessage(m)
                dialog.setNegativeButtonText(null)
                dialog.setTextColor(color)
                dialog.show()
            }
        })

        viewModel.userInfo.observe(this, Observer {
            betInfoListAdapter.isNeedRegister =
                (it == null) || (it.testFlag == TestFlag.GUEST.index)
        })
    }


    private fun initSocketObserver() {

        receiver.oddsChange.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> =
                mutableListOf()
            it.odds.forEach { map ->
                val value = map.value
                value.forEach { odd ->
                    val newOdd = org.cxct.sportlottery.network.odds.detail.Odd(
                        null,
                        odd.id,
                        null,
                        odd.odds,
                        odd.producerId,
                        odd.spread,
                        odd.status,
                    )
                    newOdd.isSelect = odd.isSelected
                    newOdd.oddState = odd.oddState
                    newList.add(newOdd)
                }
            }
            betInfoListAdapter.updatedBetInfoList = newList
        })

        receiver.matchOddsChange.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> =
                mutableListOf()
            it.odds.forEach { map ->
                val value = map.value
                value.odds?.forEach { odd ->
                    if (odd != null)
                        newList.add(odd)
                }
            }
            betInfoListAdapter.updatedBetInfoList = newList
        })

        receiver.globalStop.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val list = betInfoListAdapter.betInfoList
            list.forEach { listData ->
                if (it.producerId == null || listData.matchOdd.producerId == it.producerId) {
                    listData.matchOdd.status = BetStatus.LOCKED.code
                }
            }
            betInfoListAdapter.betInfoList = list
        })

        receiver.producerUp.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val list = betInfoListAdapter.betInfoList
            list.forEach { listData ->
                if (it.producerId == null || listData.matchOdd.producerId == it.producerId) {
                    listData.matchOdd.status = BetStatus.ACTIVATED.code
                }
            }
            betInfoListAdapter.betInfoList = list
        })

    }


    override fun onDeleteClick(position: Int) {
        //mock模式下 因為回傳內容都一樣 所以不會移除
        viewModel.removeBetInfoItem(betInfoListAdapter.betInfoList[position].matchOdd.oddsId)
        deletePosition = position
    }


    override fun onBetClick(betInfoListData: BetInfoListData, stake: Double) {
        viewModel.addBet(
            BetAddRequest(
                listOf(Odd(betInfoListData.matchOdd.oddsId, betInfoListData.matchOdd.odds)),
                listOf(Stake(betInfoListData.parlayOdds.parlayType, stake)),
                1,
                "EU"
            ), betInfoListData.matchType
        )
    }


    override fun onAddMoreClick(betInfoList: BetInfoListData) {
        val bundle = Bundle().apply {
            putString("gameType", betInfoList.matchOdd.gameType)
            putString("matchId", betInfoList.matchOdd.matchId)
            putString("matchType", betInfoList.matchType?.postValue)
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtras(bundle)
        }
        context?.startActivity(intent)
        dismiss()
    }

    override fun onRegisterClick() {
        context?.startActivity(Intent(context, RegisterActivity::class.java))
    }
}