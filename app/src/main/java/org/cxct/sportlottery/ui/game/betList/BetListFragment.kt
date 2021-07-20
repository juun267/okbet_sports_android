package org.cxct.sportlottery.ui.game.betList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.*
import kotlinx.android.synthetic.main.fragment_bet_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetListBinding
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.TextUtil

/**
 * A simple [Fragment] subclass.
 * Use the [BetListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BetListFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var binding: FragmentBetListBinding

    private val betListDiffAdapter by lazy { BetListDiffAdapter() }

    private val deleteAllLayoutAnimationListener by lazy {
        object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.llDeleteAll.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bet_list, container, false)
        binding.apply {
            gameViewModel = this@BetListFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
        initSocketObserver()

        queryData()
    }

    private fun initView() {
        initRecyclerView()
        initDeleteAllOnClickEvent()
    }

    private fun initRecyclerView() {
        binding.apply {
            rvBetList.layoutManager =
                LinearLayoutManager(this@BetListFragment.context, LinearLayoutManager.VERTICAL, false)
            rvBetList.adapter = betListDiffAdapter
        }
    }

    private fun initDeleteAllOnClickEvent() {
        binding.apply {
            btnDeleteAll.setOnClickListener {
                val enterAnimation = AnimationUtils.loadAnimation(context, R.anim.push_right_to_left_enter).apply {
                    duration = 300
                }

                llDeleteAll.visibility = View.VISIBLE
                btnDeleteAllConfirm.startAnimation(enterAnimation)
            }
            btnDeleteAllCancel.setOnClickListener {
                val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_left_to_right_exit).apply {
                    setAnimationListener(deleteAllLayoutAnimationListener)
                    duration = 300
                }

                btnDeleteAllConfirm.startAnimation(exitAnimation)
            }
        }
    }

    private fun initObserver() {
        viewModel.userMoney.observe(this.viewLifecycleOwner, {
            it.let { money -> tv_total_bet_amount.text = TextUtil.formatMoney(money ?: 0.0) }
        })

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { list ->
                betListDiffAdapter.submitList(list)
            }
        })
    }

    private fun initSocketObserver() {
        receiver.userMoney.observe(viewLifecycleOwner, {
            it?.let { money -> tv_total_bet_amount.text = TextUtil.formatMoney(money) }
        })
    }

    private fun queryData() {
        //獲取餘額
        viewModel.getMoney()
    }

    private fun showParlayDescription() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_dialog_parlay_description, null)
        val dialog = BottomSheetDialog(context ?: requireContext())
        dialog.apply {
            setContentView(bottomSheetView)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            btn_close.setOnClickListener {
                dismiss()
            }
            show()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BetListFragment.
         */
        @JvmStatic
        fun newInstance() = BetListFragment()
    }
}