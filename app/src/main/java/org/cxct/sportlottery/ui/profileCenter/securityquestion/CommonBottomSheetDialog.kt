package org.cxct.sportlottery.ui.profileCenter.securityquestion

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogBottomSelectBinding
import org.cxct.sportlottery.ui.profileCenter.profile.DialogBottomDataAdapter
import org.cxct.sportlottery.ui.profileCenter.profile.DialogBottomDataEntity

class CommonBottomSheetDialog(context: Context, val callBack: (item: DialogBottomDataEntity)-> Unit) : BottomSheetDialog(context) {

    val binding by lazy { DialogBottomSelectBinding.inflate(layoutInflater) }
    val adapter by lazy { DialogBottomDataAdapter() }
    val selectItem: DialogBottomDataEntity? =null
    init {
        initView()
    }
    fun initView(){
        binding.rvBtmData.adapter = adapter
        binding.btnBtmCancel.setOnClickListener {dismiss() }
        setContentView(binding.root)
        binding.btnBtmDone.gone()
        binding.rvBtmData.scrollToPosition(0)
        adapter.setOnItemClickListener { ater, view, position ->
            adapter.data.forEach {
                it.flag = false
            }
            val item = adapter.data[position]
            item!!.flag = true
            adapter.notifyDataSetChanged()
            binding.btnBtmDone.visible()
        }
        binding.btnBtmDone.setOnClickListener {
            dismiss()
            val select = adapter.data.firstOrNull { it.flag }
            select?.let { callBack.invoke(it) }
        }
    }
    fun setupData(list: List<DialogBottomDataEntity>,
                   title: String,
                   currStr: String?){
        binding.tvBtmTitle.text = title
        list.forEach {
            it.flag = currStr!=null && it.name == currStr
        }
        adapter.setList(list)
        if (!list.isNullOrEmpty()){
            binding.rvBtmData.scrollToPosition(0)
        }
    }
}