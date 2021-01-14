package org.cxct.sportlottery.ui.withdraw

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bank_list.*
import kotlinx.android.synthetic.main.fragment_bank_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BankListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BankListFragment : BaseFragment<WithdrawViewModel>(WithdrawViewModel::class) {

    private val mBankListAdapter by lazy {
        BankListAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bank_list, container, false).apply {
            setupRecyclerView(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            viewModel.initBankListFragment()
            setupObserve()
    }

    private fun setupObserve() {
        viewModel.bankCardList.observe(this.viewLifecycleOwner, Observer {
            it.bankCardList?.let {data ->
                mBankListAdapter.bankList = data
                if (data.isNotEmpty()){
                    tv_no_bank_card.visibility = View.GONE
                }
            }
        })
    }

    private fun setupRecyclerView(view: View) {
        view.rv_bank_list.apply{
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mBankListAdapter
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BankListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BankListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}