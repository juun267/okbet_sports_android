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
import kotlinx.android.synthetic.main.fragment_bet_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetListBinding
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.TextUtil
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BetListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BetListFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
                Timber.e("Dean, bet list = $list")
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BetListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            BetListFragment().apply {
                /*arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }*/
            }
    }
}