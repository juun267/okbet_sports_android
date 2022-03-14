package org.cxct.sportlottery.util


import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.CustomAlertDialog


/**
 * @author kevin
 * @create 2022/3/14
 * @description
 */
fun phoneNumCheckDialog(context: Context, fm: FragmentManager){
    val errorMsg = context.getString(R.string.dialog_security_need_phone)
    if (checkDialogIsShowing(fm, errorMsg)) {
        return
    }
    CustomAlertDialog(context).apply {
        setMessage(errorMsg)
        setNegativeButtonText(null)
        setCanceledOnTouchOutside(false)
        isCancelable = false
    }.show(fm, errorMsg)
}


private fun checkDialogIsShowing(fm: FragmentManager, tag: String): Boolean{
    with(fm.findFragmentByTag(tag) as DialogFragment?){
        return this!=null && dialog?.isShowing == true
    }
}