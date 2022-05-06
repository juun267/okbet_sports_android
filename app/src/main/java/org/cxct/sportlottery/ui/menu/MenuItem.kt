package org.cxct.sportlottery.ui.menu

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.menu_item.view.*
import kotlinx.android.synthetic.main.menu_item.view.tv_title
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.LanguageManager

class MenuItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ConstraintLayout(context, attrs, defStyle) {

    private var isGuest: Boolean? = null
    private var noticeCount: Int? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_item, this, false)
        addView(view)

        val typedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            view.tv_title.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            view.iv_icon.setImageResource(typedArray.getResourceId(R.styleable.CustomView_cvIcon, 0))
            view.iv_arrow.visibility = typedArray.getInt(R.styleable.CustomView_arrowSymbolVisibility, 0x00000008)
            when (LanguageManager.getSelectLanguage(context)) {
                LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> view.tv_title.letterSpacing = 0.08f
                else -> view.tv_title.letterSpacing = 0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    var text: CharSequence
        get() = tv_title.text
        set(value) {
            tv_title.text = value
        }

    fun updateLanguageImage(){
        iv_icon.setImageResource(LanguageManager.getLanguageFlag(context))
    }

    fun updateNoticeCount(noticeCount: Int) {
        this.noticeCount = noticeCount
        updateNoticeButton()
    }

    fun updateUserIdentity(isGuest: Long?) {
        this.isGuest = when (isGuest) {
            0.toLong() -> false
            1.toLong() -> true
            else -> null
        }
        updateNoticeButton()
    }

    private fun updateNoticeButton() {
        // 2022/3/11需求：不顯示通知數量，待確認不要後可拔除
//        btn_notice?.visibility = if (noticeCount ?: 0 > 0 && isGuest == false) View.VISIBLE else View.GONE
//        btn_notice?.text = if (noticeCount ?: 0 < 10) noticeCount.toString() else "N"
    }

    fun showOddsTypeChose(){
        if(llOddsType.visibility == VISIBLE){
            llOddsType.visibility = View.GONE
            ObjectAnimator.ofFloat(iv_arrow, View.ROTATION, 180f, 360f).setDuration(300).start();
        }else{
            llOddsType.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(iv_arrow, View.ROTATION, 0f, 180f).setDuration(300).start();
        }
    }
    fun setOddsEU(clickOddsTypeEU: () -> Unit) {
        tvOddsTypeEU.setOnClickListener {
            clickOddsTypeEU()
        }
    }
    fun setOddsHK(clickOddsTypeHK: () -> Unit) {
        tvOddsTypeHK.setOnClickListener {
            clickOddsTypeHK()
        }
    }
    fun setOddsMY(clickOddsTypeMY: () -> Unit) {
        tvOddsTypeMY.setOnClickListener {
            clickOddsTypeMY()
        }
    }
    fun setOddsIDN(clickOddsTypeIDN: () -> Unit) {
        tvOddsTypeIDN.setOnClickListener {
            clickOddsTypeIDN()
        }
    }
}