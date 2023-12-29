package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import android.view.*
import com.tencent.mmkv.MMKV
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.databinding.DialogAgeVerifyBinding
import org.cxct.sportlottery.util.KvUtils

class AgeVerifyDialog(val onConfirm: ()->Unit,val onExit: ()->Unit) : BaseDialog<BaseViewModel>(BaseViewModel::class) {

    init {
        setStyle(R.style.FullScreen)
    }
    companion object{
         var isAgeVerifyNeedShow :Boolean = true
             get() = KvUtils.decodeBooleanTure("isAgeVerifyNeedShow",true)
             set(value) {
                field = value
                KvUtils.put("isAgeVerifyNeedShow",value)
             }
    }
    lateinit var binding : DialogAgeVerifyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding=DialogAgeVerifyBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnConfirm.setOnClickListener {
            dismiss()
            onConfirm.invoke()
        }
        binding.btnExit.setOnClickListener {
            dismiss()
            onExit.invoke()
        }
    }

}
