package org.cxct.sportlottery.ui.promotion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogRewardHistoryBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel

class RewardHistoryDialog: BaseDialog<MainHomeViewModel>(MainHomeViewModel::class) {

    init {
        setStyle(R.style.FullScreen)
    }
    lateinit var binding : DialogRewardHistoryBinding
    private val adapter by lazy { RewardHistoryAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DialogRewardHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserve()
    }
    private fun initView()=binding.run {
        ivClose.setOnClickListener {
            dismiss()
        }
        rvRecord.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        rvRecord.adapter = adapter
    }

    private fun initObserve() = viewModel.run {
        adapter.setList(listOf("","","","",""))
//        sendCodeResult.observe(viewLifecycleOwner) { smsResult-> // 发送验证码
//            hideLoading()
//
//            if (smsResult.succeeded()) {
//                userName = smsResult.getData()?.userName
//                ToastUtil.showToast(requireActivity(), smsResult.getData()?.msg)
//                codeCountDown()
//                return@observe
//            }
//
//            ToastUtil.showToast(requireActivity(), smsResult.msg)
//            binding.btnSendSms.setBtnEnable(true)
//        }

    }


}