package org.cxct.sportlottery.ui.login.signUp.info

import android.content.Context
import androidx.core.content.ContextCompat
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import org.cxct.sportlottery.R

class StringPickerOptions(val context: Context){


    fun getBuilder(listener:OnOptionsSelectListener):OptionsPickerBuilder{

        return OptionsPickerBuilder(context,listener)
            .setCancelText(" ")
            .setTitleColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_CCCCCC_000000
                )
            )
            .setTitleBgColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_2B2B2B_e2e2e2
                )
            )
            .setBgColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_191919_FCFCFC
                )
            )
            .setSubmitColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_7F7F7F_999999
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_7F7F7F_999999
                )
            )
            .isDialog(false)
    }
}