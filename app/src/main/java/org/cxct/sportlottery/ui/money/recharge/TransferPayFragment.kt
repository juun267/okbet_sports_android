package org.cxct.sportlottery.ui.money.recharge

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R

class TransferPayFragment : Fragment() {

    companion object {
        fun newInstance() = TransferPayFragment()
    }

    private lateinit var viewModel: TransferPayViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.transfer_pay_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TransferPayViewModel::class.java)
        // TODO: Use the ViewModel
    }

}